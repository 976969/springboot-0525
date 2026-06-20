package com.qcby.springboot_0525.service;

import com.qcby.springboot_0525.common.BusinessException;
import com.qcby.springboot_0525.entity.EvaluationRecord;
import com.qcby.springboot_0525.entity.EvaluationReport;
import com.qcby.springboot_0525.entity.TrainingResult;
import com.qcby.springboot_0525.mapper.EvaluationRecordMapper;
import com.qcby.springboot_0525.mapper.EvaluationReportMapper;
import com.qcby.springboot_0525.mapper.TrainingResultMapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 报告服务 - 生成和管理评价报告
 */
@Service
public class ReportService {

    @Resource
    private EvaluationReportMapper reportMapper;

    @Resource
    private EvaluationRecordMapper recordMapper;

    @Resource
    private TrainingResultMapper resultMapper;

    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * 生成单个学生的评价报告
     * @param resultId 成果ID
     * @return 生成的报告
     */
    @Transactional
    public EvaluationReport generateReport(Long resultId) {
        // 1. 查询成果信息
        TrainingResult result = resultMapper.selectById(resultId);
        if (result == null) {
            throw new BusinessException("成果不存在");
        }

        // 2. 查询该成果的所有评价记录
        List<EvaluationRecord> records = recordMapper.selectByResultId(resultId);
        if (records == null || records.isEmpty()) {
            throw new BusinessException("该成果暂无评价记录，请先生成评价");
        }

        // 3. 计算总分（取最终得分的平均值）
        BigDecimal totalScore = calculateTotalScore(records);

        // 4. 构建报告数据（JSON格式）
        String reportData = buildReportData(result, records, totalScore);

        // 5. 检查是否已存在报告
        EvaluationReport existingReport = findExistingReport(result.getTaskId(), result.getStudentId());
        
        if (existingReport != null) {
            // 更新已有报告
            existingReport.setTotalScore(totalScore);
            existingReport.setReportData(reportData);
            existingReport.setCreateTime(new Date());
            reportMapper.updateById(existingReport);
            return existingReport;
        } else {
            // 创建新报告
            EvaluationReport report = new EvaluationReport();
            report.setTaskId(result.getTaskId());
            report.setStudentId(result.getStudentId());
            report.setTotalScore(totalScore);
            report.setReportData(reportData);
            report.setCreateTime(new Date());
            reportMapper.insert(report);
            return report;
        }
    }

    /**
     * 批量生成任务下所有学生的报告（并行处理）
     * @param taskId 任务ID
     * @return 生成的报告数量
     */
    @Transactional
    public int generateReportsByTask(Long taskId) {
        // 查询该任务下所有已提交的成果
        List<TrainingResult> results = resultMapper.selectByTaskId(taskId);
        
        if (results == null || results.isEmpty()) {
            throw new BusinessException("该任务下暂无提交的成果");
        }
        
        // 使用线程池并行处理（固定线程数为CPU核心数）
        int threadCount = Math.min(results.size(), Runtime.getRuntime().availableProcessors());
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        
        AtomicInteger successCount = new AtomicInteger(0);
        List<CompletableFuture<Void>> futures = new ArrayList<>();
        
        for (TrainingResult result : results) {
            CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
                try {
                    // 每个线程独立生成报告
                    generateReport(result.getId());
                    successCount.incrementAndGet();
                } catch (Exception e) {
                    System.err.println("生成报告失败 - 成果ID: " + result.getId() + ", 原因: " + e.getMessage());
                }
            }, executor);
            
            futures.add(future);
        }
        
        // 等待所有任务完成
        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
        
        // 关闭线程池
        executor.shutdown();
        
        return successCount.get();
    }

    /**
     * 计算总分
     */
    private BigDecimal calculateTotalScore(List<EvaluationRecord> records) {
        BigDecimal sum = BigDecimal.ZERO;
        int count = 0;
        
        for (EvaluationRecord record : records) {
            // 优先使用最终得分，其次教师评分，最后AI评分
            BigDecimal score = null;
            if (record.getFinalScore() != null) {
                score = record.getFinalScore();
            } else if (record.getTeacherScore() != null && record.getTeacherScore().compareTo(BigDecimal.ZERO) > 0) {
                score = record.getTeacherScore();
            } else if (record.getAiScore() != null) {
                score = record.getAiScore();
            }
            
            if (score != null) {
                sum = sum.add(score);
                count++;
            }
        }
        
        if (count == 0) {
            return BigDecimal.ZERO;
        }
        
        return sum.divide(new BigDecimal(count), 2, RoundingMode.HALF_UP);
    }

    /**
     * 构建报告数据（JSON）
     */
    private String buildReportData(TrainingResult result, List<EvaluationRecord> records, BigDecimal totalScore) {
        try {
            Map<String, Object> reportMap = new HashMap<>();
            
            // 基本信息
            Map<String, Object> basicInfo = new HashMap<>();
            basicInfo.put("fileName", result.getFileName());
            basicInfo.put("submitTime", result.getUploadTime());
            basicInfo.put("totalScore", totalScore);
            reportMap.put("basicInfo", basicInfo);
            
            // 各指标详情
            List<Map<String, Object>> indicatorDetails = new ArrayList<>();
            for (EvaluationRecord record : records) {
                Map<String, Object> detail = new HashMap<>();
                detail.put("indicatorName", record.getIndicatorName());
                detail.put("aiScore", record.getAiScore());
                detail.put("teacherScore", record.getTeacherScore());
                detail.put("finalScore", record.getFinalScore());
                detail.put("aiComment", record.getAiComment());
                detail.put("teacherComment", record.getTeacherComment());
                indicatorDetails.add(detail);
            }
            reportMap.put("indicators", indicatorDetails);
            
            return objectMapper.writeValueAsString(reportMap);
        } catch (Exception e) {
            throw new BusinessException("报告数据构建失败: " + e.getMessage());
        }
    }

    /**
     * 查找已存在的报告
     */
    private EvaluationReport findExistingReport(Long taskId, Long studentId) {
        List<EvaluationReport> reports = reportMapper.selectByTaskAndStudent(taskId, studentId);
        return reports != null && !reports.isEmpty() ? reports.get(0) : null;
    }

    /**
     * 获取报告的成果ID
     */
    public Long getResultIdByReport(EvaluationReport report) {
        TrainingResult result = resultMapper.selectByTaskAndStudent(report.getTaskId(), report.getStudentId());
        if (result == null) {
            throw new BusinessException("找不到对应的成果");
        }
        return result.getId();
    }

    /**
     * 删除报告
     */
    @Transactional
    public void deleteReport(Long reportId) {
        reportMapper.deleteById(reportId);
    }
}
