package com.qcby.springboot_0525.controller;

import com.qcby.springboot_0525.common.BusinessException;
import com.qcby.springboot_0525.common.Result;
import com.qcby.springboot_0525.entity.CheckRecord;
import com.qcby.springboot_0525.entity.TrainingResult;
import com.qcby.springboot_0525.entity.TrainingTask;
import com.qcby.springboot_0525.mapper.CheckRecordMapper;
import com.qcby.springboot_0525.service.LlmService;
import com.qcby.springboot_0525.service.TrainingResultService;
import com.qcby.springboot_0525.service.TrainingTaskService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

/**
 * 智能核查控制器（AI辅助核查、大模型调用）
 */
@RestController
@RequestMapping("/check")
public class CheckController {

    private static final Logger log = LoggerFactory.getLogger(CheckController.class);

    @Resource
    private TrainingResultService resultService;

    @Resource
    private TrainingTaskService taskService;

    @Resource
    private LlmService llmService;

    @Resource
    private CheckRecordMapper checkRecordMapper;

    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * 对指定实训成果进行智能核查（三维度拆分）
     */
    @PostMapping("/run/{resultId}")
    public Result<List<CheckRecord>> checkResult(@PathVariable Long resultId) {
        TrainingResult result = resultService.getById(resultId);
        if (result == null) {
            throw new BusinessException("实训成果不存在");
        }
        if (result.getParsedContent() == null || result.getParsedContent().isEmpty()) {
            throw new BusinessException("实训成果内容为空，无法核查");
        }

        TrainingTask task = taskService.getById(result.getTaskId());
        if (task == null) {
            throw new BusinessException("关联的实训任务不存在");
        }

        // 先清除该成果的旧核查记录
        checkRecordMapper.deleteByResultId(resultId);

        try {
            // 调用大模型核查
            String llmResult = llmService.checkTraining(
                    task.getRequirements() != null ? task.getRequirements() : task.getDescription(),
                    result.getParsedContent()
            );

            log.info("AI核查原始返回: {}", llmResult);

            // 提取JSON部分（处理大模型可能返回的额外文本）
            String jsonStr = extractJson(llmResult);
            
            JsonNode root;
            try {
                root = objectMapper.readTree(jsonStr);
            } catch (Exception parseEx) {
                log.error("JSON解析失败，原始数据: {}", jsonStr);
                log.error("解析异常: ", parseEx);
                // 尝试修复常见的JSON问题
                jsonStr = fixCommonJsonIssues(jsonStr);
                try {
                    root = objectMapper.readTree(jsonStr);
                } catch (Exception finalEx) {
                    log.error("修复后仍解析失败", finalEx);
                    // 返回默认的核查结果
                    return generateDefaultCheckResult(resultId, "JSON解析失败，请重新核查");
                }
            }

            // 三个维度的核查记录
            String[][] dimensions = {
                    {"completeness", "完整性"},
                    {"logic", "逻辑性"},
                    {"match", "匹配度"}
            };

            for (String[] dim : dimensions) {
                String dimKey = dim[0];
                String dimName = dim[1];

                CheckRecord record = new CheckRecord();
                record.setResultId(resultId);
                record.setCheckType(dimKey);

                JsonNode dimNode = root.path(dimKey);
                if (dimNode.isMissingNode() || dimNode.isNull()) {
                    // 某维度解析失败
                    record.setCheckResult(2);
                    record.setIssueDescription(dimName + "核查数据缺失");
                    record.setSuggestion("请重新核查");
                } else {
                    int score = dimNode.path("score").asInt(0);
                    String issue = dimNode.path("issue").asText("");
                    String suggestion = dimNode.path("suggestion").asText("");

                    // 60分及以上为通过(1)，60分以下为存在问题(2)
                    record.setCheckResult(score >= 60 ? 1 : 2);
                    record.setIssueDescription(issue);
                    record.setSuggestion(suggestion);
                }
                checkRecordMapper.insert(record);
            }

            // 更新成果状态为已核查
            resultService.updateStatus(resultId, 1);

            return Result.success(checkRecordMapper.selectByResultId(resultId));
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("智能核查失败", e);
            throw new BusinessException("智能核查失败：" + e.getMessage());
        }
    }

    /**
     * 修复常见的JSON问题
     */
    private String fixCommonJsonIssues(String json) {
        // 移除所有单引号,替换为双引号(如果是键值对)
        // 但保留字符串内容中的单引号
        json = json.replaceAll("'([^']+)'", "\"$1\"");
        
        // 移除数字周围的引号
        json = json.replaceAll("\"(\\d+)\"", "$1");
        
        // 修复true/false/null的引号
        json = json.replaceAll("\"(true|false|null)\"", "$1");
        
        // 移除多余的换行和空格
        json = json.replaceAll("\\s+", " ");
        
        return json;
    }
    
    /**
     * 生成默认的核查结果(当JSON解析失败时)
     */
    private Result<List<CheckRecord>> generateDefaultCheckResult(Long resultId, String errorMsg) {
        log.warn("生成默认核查结果: {}", errorMsg);
        
        String[] dimensions = {"completeness", "logic", "match"};
        String[] dimNames = {"完整性", "逻辑性", "匹配度"};
        
        for (int i = 0; i < dimensions.length; i++) {
            CheckRecord record = new CheckRecord();
            record.setResultId(resultId);
            record.setCheckType(dimensions[i]);
            record.setCheckResult(2); // 2=存在问题
            record.setIssueDescription(dimNames[i] + "核查失败: " + errorMsg);
            record.setSuggestion("请重新核查或联系管理员");
            checkRecordMapper.insert(record);
        }
        
        // 更新状态为已核查(即使失败)
        resultService.updateStatus(resultId, 1);
        
        return Result.success(checkRecordMapper.selectByResultId(resultId));
    }

    /**
     * 获取指定成果的核查记录
     */
    @GetMapping("/records/{resultId}")
    public Result<List<CheckRecord>> getCheckRecords(@PathVariable Long resultId) {
        return Result.success(checkRecordMapper.selectByResultId(resultId));
    }

    /**
     * 从大模型返回文本中提取JSON字符串
     */
    private String extractJson(String text) {
        if (text == null || text.isEmpty()) {
            throw new BusinessException("大模型返回为空");
        }
        
        // 尝试找到 JSON 对象的起止位置
        int start = text.indexOf('{');
        int end = text.lastIndexOf('}');
        if (start >= 0 && end > start) {
            String jsonStr = text.substring(start, end + 1);
            // 清理JSON中的非法字符
            jsonStr = cleanJson(jsonStr);
            return jsonStr;
        }
        
        // 如果没有找到{},尝试清理整个文本
        String cleaned = cleanJson(text);
        // 再次尝试查找JSON
        start = cleaned.indexOf('{');
        end = cleaned.lastIndexOf('}');
        if (start >= 0 && end > start) {
            return cleaned.substring(start, end + 1);
        }
        
        throw new BusinessException("无法从大模型返回中提取JSON格式");
    }
    
    /**
     * 清理JSON字符串中的非法字符
     */
    private String cleanJson(String json) {
        if (json == null) return "";
        
        // 移除BOM
        if (json.startsWith("\uFEFF")) {
            json = json.substring(1);
        }
        
        // 移除零宽字符
        json = json.replaceAll("[\u200B-\u200D\uFEFF]", "");
        
        // 替换中文标点为英文标点
        json = json.replace("，", ",");
        json = json.replace("。", ".");
        json = json.replace("：", ":");
        json = json.replace("；", ";");
        json = json.replace("“", "\"");
        json = json.replace("”", "\"");
        json = json.replace("‘", "'");
        json = json.replace("’", "'");
        
        // 移除控制字符(除了换行和回车)
        json = json.replaceAll("[\\x00-\\x09\\x0B\\x0C\\x0E-\\x1F]", "");
        
        // 修复可能的转义问题
        json = json.replace("\\\\\"", "\""); // 双重转义
        json = json.replace("\\\"", "\""); // 转义引号
        
        // 移除末尾多余逗号
        json = json.replaceAll(",\\s*([\\]}])", "$1");
        
        return json.trim();
    }
}
