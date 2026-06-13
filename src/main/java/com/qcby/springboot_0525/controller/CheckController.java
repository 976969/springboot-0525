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
            JsonNode root = objectMapper.readTree(jsonStr);

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
        // 尝试找到 JSON 对象的起止位置
        int start = text.indexOf('{');
        int end = text.lastIndexOf('}');
        if (start >= 0 && end > start) {
            return text.substring(start, end + 1);
        }
        return text;
    }
}
