package com.qcby.springboot_0525.controller;

import cn.dev33.satoken.stp.StpUtil;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.qcby.springboot_0525.common.BusinessException;
import com.qcby.springboot_0525.common.PageResult;
import com.qcby.springboot_0525.common.Result;
import com.qcby.springboot_0525.entity.EvaluationIndicator;
import com.qcby.springboot_0525.service.EvaluationIndicatorService;
import com.qcby.springboot_0525.service.LlmService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.*;

/**
 * 评价指标控制器（指标管理、AI生成、权重重置）
 */
@RestController
@RequestMapping("/indicator")
public class EvaluationIndicatorController {

    private static final Logger log = LoggerFactory.getLogger(EvaluationIndicatorController.class);

    @Resource
    private EvaluationIndicatorService indicatorService;

    @Resource
    private LlmService llmService;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @GetMapping("/page")
    public Result<PageResult<EvaluationIndicator>> list(
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(required = false) Long teacherId) {
        
        String role = (String) StpUtil.getSession().get("role");
        Object realIdObj = StpUtil.getSession().get("realId");
        
        List<EvaluationIndicator> list;
        
        if ("teacher".equals(role) && realIdObj != null) {
            Long tid = Long.valueOf(realIdObj.toString());
            // 首次登录时初始化指标
            indicatorService.initTeacherIndicators(tid);
            // 教师：只看自己的指标
            list = indicatorService.listByTeacherId(tid);
        } else {
            // 管理员：看所有指标
            PageHelper.startPage(pageNum, pageSize);
            list = indicatorService.list();
            // 管理员可按教师筛选
            if (teacherId != null) {
                list = list.stream().filter(i -> teacherId.equals(i.getTeacherId())).collect(java.util.stream.Collectors.toList());
            }
        }
        
        PageInfo<EvaluationIndicator> pageInfo = new PageInfo<>(list);
        return Result.success(PageResult.of(pageInfo.getList(), pageInfo.getTotal(), pageNum, pageSize));
    }

    @GetMapping("/list")
    public Result<List<EvaluationIndicator>> listAll() {
        String role = (String) StpUtil.getSession().get("role");
        Object realIdObj = StpUtil.getSession().get("realId");
        
        List<EvaluationIndicator> list;
        
        if ("teacher".equals(role) && realIdObj != null) {
            Long teacherId = Long.valueOf(realIdObj.toString());
            // 首次登录时初始化指标
            indicatorService.initTeacherIndicators(teacherId);
            // 教师：只看自己的指标
            list = indicatorService.listByTeacherId(teacherId);
        } else {
            // 管理员：看所有指标
            list = indicatorService.list();
        }
        
        return Result.success(list);
    }

    @GetMapping("/{id}")
    public Result<EvaluationIndicator> getById(@PathVariable Long id) {
        return Result.success(indicatorService.getById(id));
    }

    @PostMapping
    public Result<Void> add(@RequestBody EvaluationIndicator indicator) {
        String role = (String) StpUtil.getSession().get("role");
        Object realIdObj = StpUtil.getSession().get("realId");
        
        // 教师只能创建自定义指标
        if ("teacher".equals(role) && realIdObj != null) {
            Long tid = Long.valueOf(realIdObj.toString());
            indicator.setTeacherId(tid);
            indicator.setIsSystem(0);
        }
        // 管理员可以创建系统指标（teacher_id=0）或自定义指标
        
        indicatorService.add(indicator);
        return Result.success();
    }

    @PutMapping
    public Result<Void> update(@RequestBody EvaluationIndicator indicator) {
        String role = (String) StpUtil.getSession().get("role");
        Object realIdObj = StpUtil.getSession().get("realId");
        
        if ("teacher".equals(role) && realIdObj != null) {
            Long tid = Long.valueOf(realIdObj.toString());
            // 教师只能编辑自己的指标
            EvaluationIndicator existing = indicatorService.getById(indicator.getId());
            if (existing == null || !tid.equals(existing.getTeacherId())) {
                throw new BusinessException(403, "无权限编辑该指标");
            }
        }
        // 管理员可以编辑所有指标
        
        indicatorService.update(indicator);
        return Result.success();
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        String role = (String) StpUtil.getSession().get("role");
        
        EvaluationIndicator indicator = indicatorService.getById(id);
        if (indicator == null) {
            throw new BusinessException("指标不存在");
        }
        
        if ("teacher".equals(role)) {
            // 教师不能删除系统指标
            if (indicator.getIsSystem() != null && indicator.getIsSystem() == 1) {
                throw new BusinessException("系统指标不可删除");
            }
            // 教师只能删除自己的指标
            Object realIdObj = StpUtil.getSession().get("realId");
            if (realIdObj != null) {
                Long tid = Long.valueOf(realIdObj.toString());
                if (!tid.equals(indicator.getTeacherId())) {
                    throw new BusinessException(403, "无权限删除该指标");
                }
            }
        }
        // 管理员可以删除所有指标（包括系统指标）
        
        indicatorService.delete(id);
        return Result.success();
    }

    @PostMapping("/reset")
    public Result<Void> resetWeights() {
        String role = (String) StpUtil.getSession().get("role");
        Object realIdObj = StpUtil.getSession().get("realId");
        
        if ("teacher".equals(role) && realIdObj != null) {
            Long teacherId = Long.valueOf(realIdObj.toString());
            // 教师：只重置自己的指标
            indicatorService.resetTeacherWeights(teacherId);
        } else {
            // 管理员：重置所有指标
            indicatorService.resetWeights();
        }
        
        return Result.success();
    }

    /**
     * 查询系统标准指标（teacher_id=0）
     */
    @GetMapping("/system")
    public Result<List<EvaluationIndicator>> getSystemIndicators() {
        return Result.success(indicatorService.getSystemTemplate());
    }

    /**
     * 查询指定教师的自定义指标
     */
    @GetMapping("/by-teacher/{teacherId}")
    public Result<List<EvaluationIndicator>> getTeacherCustomIndicators(@PathVariable Long teacherId) {
        List<EvaluationIndicator> list = indicatorService.listByTeacherId(teacherId);
        // 只返回自定义指标
        list = list.stream()
            .filter(i -> i.getIsSystem() != null && i.getIsSystem() == 0)
            .collect(java.util.stream.Collectors.toList());
        return Result.success(list);
    }

    /**
     * AI智能生成评价指标配置
     * 根据教师描述自动生成或调整指标及权重
     */
    @PostMapping("/ai-generate")
    public Result<Map<String, Object>> aiGenerate(@RequestBody Map<String, String> params) {
        String requirements = params.get("requirements");
        if (requirements == null || requirements.trim().isEmpty()) {
            throw new BusinessException("请输入评分要求");
        }

        // 获取现有指标并格式化为描述文本
        List<EvaluationIndicator> existingList = indicatorService.list();
        StringBuilder sb = new StringBuilder();
        for (EvaluationIndicator ind : existingList) {
            sb.append(String.format("- %s (id=%d, 分类=%s, 权重=%s, 类型=%s)\n",
                    ind.getName(), ind.getId(),
                    ind.getCategory() != null ? ind.getCategory() : "无",
                    ind.getDefaultWeight(),
                    ind.getIsSystem() != null && ind.getIsSystem() == 1 ? "系统指标" : "自定义"));
        }
        String existingText = sb.toString();

        try {
            // 调用大模型生成配置
            String llmResult = llmService.generateIndicators(requirements, existingText);
            log.info("AI生成指标原始返回: {}", llmResult);

            // 提取并清理JSON
            String jsonStr = extractJson(llmResult);
            jsonStr = cleanJson(jsonStr);

            JsonNode root;
            try {
                root = objectMapper.readTree(jsonStr);
            } catch (Exception parseEx) {
                log.error("AI返回JSON解析失败, 原始数据: {}", jsonStr);
                throw new BusinessException("AI返回数据格式异常，请重新尝试");
            }
            JsonNode indicatorsNode = root.path("indicators");
            String suggestion = root.path("suggestion").asText("");

            if (!indicatorsNode.isArray() || indicatorsNode.isEmpty()) {
                throw new BusinessException("AI未返回有效的指标配置");
            }

            // 建立现有指标名称映射
            Map<String, EvaluationIndicator> nameMap = new HashMap<>();
            for (EvaluationIndicator ind : existingList) {
                nameMap.put(ind.getName(), ind);
            }

            // 记录处理过的系统指标
            Set<Long> processedSystemIds = new HashSet<>();
            List<String> actions = new ArrayList<>();

            // 遍历AI返回的指标
            for (JsonNode node : indicatorsNode) {
                String name = node.path("name").asText("").trim();
                String desc = node.path("description").asText("").trim();
                String category = node.path("category").asText("").trim();
                BigDecimal weight = new BigDecimal(node.path("weight").asText("20"));
                int isSystem = node.path("isSystem").asInt(0);

                if (name.isEmpty()) continue;

                EvaluationIndicator existing = nameMap.get(name);

                if (existing != null) {
                    // 更新已存在的指标
                    existing.setDefaultWeight(weight);
                    if (!desc.isEmpty()) existing.setDescription(desc);
                    if (!category.isEmpty()) existing.setCategory(category);
                    indicatorService.update(existing);
                    processedSystemIds.add(existing.getId());
                    actions.add("更新: " + name + " (权重=" + weight + ")");
                } else if (isSystem == 1) {
                    // AI试图创建系统指标 → 作为自定义指标新增
                    EvaluationIndicator newInd = new EvaluationIndicator();
                    newInd.setName(name);
                    newInd.setDescription(desc);
                    newInd.setCategory(category);
                    newInd.setDefaultWeight(weight);
                    newInd.setOriginalWeight(weight);
                    newInd.setIsSystem(0);
                    indicatorService.add(newInd);
                    actions.add("新增(自定义): " + name + " (权重=" + weight + ")");
                } else {
                    // 新增自定义指标
                    EvaluationIndicator newInd = new EvaluationIndicator();
                    newInd.setName(name);
                    newInd.setDescription(desc);
                    newInd.setCategory(category);
                    newInd.setDefaultWeight(weight);
                    newInd.setOriginalWeight(weight);
                    newInd.setIsSystem(0);
                    indicatorService.add(newInd);
                    actions.add("新增: " + name + " (权重=" + weight + ")");
                }
            }

            // 确保所有系统指标都被保留（未被AI提及的保持原样）
            for (EvaluationIndicator ind : existingList) {
                if (ind.getIsSystem() != null && ind.getIsSystem() == 1 && !processedSystemIds.contains(ind.getId())) {
                    actions.add("保留: " + ind.getName() + " (系统指标, 权重=" + ind.getDefaultWeight() + ")");
                }
            }

            Map<String, Object> result = new HashMap<>();
            result.put("suggestion", suggestion);
            result.put("actions", actions);
            result.put("indicators", indicatorService.list());

            return Result.success("AI指标配置生成完成", result);

        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("AI生成指标失败", e);
            throw new BusinessException("AI生成失败: " + e.getMessage());
        }
    }

    private String extractJson(String text) {
        if (text == null) return "{}";
        // 移除markdown代码块标记
        text = text.replaceAll("```json\\s*", "").replaceAll("```\\s*", "");
        int start = text.indexOf('{');
        int end = text.lastIndexOf('}');
        if (start >= 0 && end > start) {
            return text.substring(start, end + 1);
        }
        return text;
    }

    /**
     * 清理JSON字符串中的非法字符（复用核查模块的容错逻辑）
     */
    private String cleanJson(String json) {
        if (json == null) return "{}";
        // 移除BOM
        if (json.startsWith("\uFEFF")) json = json.substring(1);
        // 移除零宽字符
        json = json.replaceAll("[\u200B-\u200D\uFEFF]", "");
        // 替换中文标点
        json = json.replace("\uff0c", ",").replace("\u3002", ".").replace("\uff1a", ":")
                   .replace("\uff1b", ";").replace("\u201c", "\"").replace("\u201d", "\"");
        // 移除控制字符(除换行和回车)
        json = json.replaceAll("[\u0000-\u0009\u000B\u000C\u000E-\u001F]", "");
        // 移除末尾多余逗号
        json = json.replaceAll(",\\s*([\\]}])", "$1");
        // 截断重复乱码字符（如连续的"is"重复）
        json = json.replaceAll("(.{5,})\\1{3,}", "");
        return json.trim();
    }
}
