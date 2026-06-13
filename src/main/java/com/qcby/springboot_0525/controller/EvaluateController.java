package com.qcby.springboot_0525.controller;

import cn.dev33.satoken.stp.StpUtil;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.qcby.springboot_0525.common.BusinessException;
import com.qcby.springboot_0525.common.PageResult;
import com.qcby.springboot_0525.common.Result;
import com.qcby.springboot_0525.entity.*;
import com.qcby.springboot_0525.mapper.EvaluationRecordMapper;
import com.qcby.springboot_0525.mapper.EvaluationReportMapper;
import com.qcby.springboot_0525.service.EvaluationIndicatorService;
import com.qcby.springboot_0525.service.LlmService;
import com.qcby.springboot_0525.service.TrainingResultService;
import com.qcby.springboot_0525.service.TrainingTaskService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/evaluate")
public class EvaluateController {

    private static final Logger log = LoggerFactory.getLogger(EvaluateController.class);

    @Resource
    private TrainingResultService resultService;

    @Resource
    private TrainingTaskService taskService;

    @Resource
    private EvaluationIndicatorService indicatorService;

    @Resource
    private LlmService llmService;

    @Resource
    private EvaluationRecordMapper recordMapper;

    @Resource
    private EvaluationReportMapper reportMapper;

    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * AI自动评分 - 对指定成果的所有指标进行评分
     */
    @PostMapping("/ai/{resultId}")
    public Result<Map<String, Object>> aiEvaluate(@PathVariable Long resultId) {
        TrainingResult result = resultService.getById(resultId);
        if (result == null) {
            throw new BusinessException("实训成果不存在");
        }

        TrainingTask task = taskService.getById(result.getTaskId());
        List<EvaluationIndicator> indicators = indicatorService.list();

        String requirements = task.getRequirements() != null ? task.getRequirements() : task.getDescription();
        String content = result.getParsedContent();

        if (content == null || content.isEmpty()) {
            throw new BusinessException("成果内容为空，无法评价");
        }

        // 清除该成果的旧评价记录
        recordMapper.deleteByResultId(resultId);

        int successCount = 0;
        int failCount = 0;
        List<String> failMessages = new ArrayList<>();

        // 逐指标调用大模型评分
        for (EvaluationIndicator indicator : indicators) {
            try {
                String llmResult = llmService.evaluateByIndicator(
                        indicator.getName(), indicator.getDescription(), requirements, content
                );

                log.info("指标[{}] AI返回: {}", indicator.getName(), llmResult);

                // 提取JSON部分（容错处理）
                String jsonStr = extractJson(llmResult);
                JsonNode scoreNode = objectMapper.readTree(jsonStr);

                BigDecimal aiScore = new BigDecimal(scoreNode.path("score").asText("0"));
                String aiComment = scoreNode.path("comment").asText("无评语");

                // 评分范围校验
                if (aiScore.compareTo(BigDecimal.ZERO) < 0) aiScore = BigDecimal.ZERO;
                if (aiScore.compareTo(new BigDecimal(100)) > 0) aiScore = new BigDecimal(100);

                EvaluationRecord record = new EvaluationRecord();
                record.setResultId(resultId);
                record.setIndicatorId(indicator.getId());
                record.setAiScore(aiScore);
                record.setAiComment(aiComment);
                record.setFinalScore(aiScore); // 默认最终得分=AI评分
                recordMapper.insert(record);
                successCount++;
            } catch (Exception e) {
                // 评分失败不写入脏数据，跳过该指标
                log.warn("指标[{}] AI评分失败: {}", indicator.getName(), e.getMessage());
                failCount++;
                failMessages.add(indicator.getName() + ": " + e.getMessage());
            }
        }

        // 只有全部成功时才更新状态为已评价，部分成功则更新为已核查
        if (failCount == 0) {
            resultService.updateStatus(resultId, 2); // 已评价
        } else if (successCount > 0) {
            resultService.updateStatus(resultId, 1); // 已核查（部分评分）
        }

        Map<String, Object> resultMap = new HashMap<>();
        resultMap.put("records", recordMapper.selectByResultId(resultId));
        resultMap.put("successCount", successCount);
        resultMap.put("failCount", failCount);
        resultMap.put("failMessages", failMessages);

        return Result.success("AI评分完成（成功" + successCount + "个，失败" + failCount + "个）", resultMap);
    }

    /**
     * 从大模型返回文本中提取JSON字符串
     */
    private String extractJson(String text) {
        int start = text.indexOf('{');
        int end = text.lastIndexOf('}');
        if (start >= 0 && end > start) {
            return text.substring(start, end + 1);
        }
        return text;
    }

    /**
     * 获取指定成果的评价记录
     */
    @GetMapping("/records/{resultId}")
    public Result<List<EvaluationRecord>> getRecords(@PathVariable Long resultId) {
        return Result.success(recordMapper.selectByResultId(resultId));
    }

    /**
     * 教师评分 - 修改某条评价记录的教师分数和评语
     */
    @PutMapping("/teacher-score")
    public Result<Void> teacherScore(@RequestBody Map<String, Object> params) {
        Long recordId = Long.valueOf(params.get("id").toString());
        BigDecimal teacherScore = new BigDecimal(params.get("teacherScore").toString());
        String teacherComment = params.get("teacherComment").toString();

        EvaluationRecord record = recordMapper.selectById(recordId);
        if (record == null) {
            throw new BusinessException("评价记录不存在");
        }

        record.setTeacherScore(teacherScore);
        record.setTeacherComment(teacherComment);
        record.setFinalScore(teacherScore); // 教师评分覆盖最终得分
        recordMapper.updateById(record);

        return Result.success();
    }

    /**
     * 生成评价报告
     */
    @PostMapping("/report/{resultId}")
    public Result<EvaluationReport> generateReport(@PathVariable Long resultId) {
        TrainingResult result = resultService.getById(resultId);
        List<EvaluationRecord> records = recordMapper.selectByResultId(resultId);

        if (records.isEmpty()) {
            throw new BusinessException("暂无评价数据，请先进行评价");
        }

        // 计算加权总分
        BigDecimal totalScore = BigDecimal.ZERO;
        BigDecimal totalWeight = BigDecimal.ZERO;
        StringBuilder evalData = new StringBuilder();

        for (EvaluationRecord record : records) {
            BigDecimal weight = record.getIndicatorWeight() != null ? record.getIndicatorWeight() : BigDecimal.ONE;
            BigDecimal score = record.getFinalScore() != null ? record.getFinalScore() : BigDecimal.ZERO;
            // 加权: score * weight / 100
            totalScore = totalScore.add(score.multiply(weight).divide(new BigDecimal(100), 2, RoundingMode.HALF_UP));
            totalWeight = totalWeight.add(weight);
            evalData.append(record.getIndicatorName()).append(": ")
                    .append(score).append("分 (权重").append(weight).append("%) - ")
                    .append(record.getAiComment()).append("\n");
        }

        // 生成报告总结
        String summary = "";
        try {
            summary = llmService.generateReportSummary(evalData.toString());
        } catch (Exception e) {
            summary = "报告生成失败";
        }

        // 保存报告
        Map<String, Object> reportData = new HashMap<>();
        reportData.put("totalScore", totalScore);
        reportData.put("records", records);
        reportData.put("summary", summary);

        EvaluationReport report = new EvaluationReport();
        report.setTaskId(result.getTaskId());
        report.setStudentId(result.getStudentId());
        report.setTotalScore(totalScore);
        try {
            report.setReportData(objectMapper.writeValueAsString(reportData));
        } catch (Exception e) {
            report.setReportData("{}");
        }
        reportMapper.insert(report);

        return Result.success(report);
    }

    /**
     * 获取评价报告列表
     */
    @GetMapping("/report/list")
    public Result<List<EvaluationReport>> reportList() {
        return Result.success(reportMapper.selectList());
    }

    /**
     * 分页获取评价记录
     */
    @GetMapping("/records/page")
    public Result<PageResult<EvaluationRecord>> listRecords(
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize) {
        PageHelper.startPage(pageNum, pageSize);
        
        List<EvaluationRecord> list = recordMapper.selectList();
        PageInfo<EvaluationRecord> pageInfo = new PageInfo<>(list);
        return Result.success(PageResult.of(pageInfo.getList(), pageInfo.getTotal(), pageNum, pageSize));
    }
}
