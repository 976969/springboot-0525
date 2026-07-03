package com.qcby.springboot_0525.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import cn.dev33.satoken.annotation.SaCheckRole;
import cn.dev33.satoken.stp.StpUtil;
import com.qcby.springboot_0525.common.BusinessException;
import com.qcby.springboot_0525.common.PageResult;
import com.qcby.springboot_0525.common.Result;
import com.qcby.springboot_0525.entity.EvaluationRecord;
import com.qcby.springboot_0525.entity.EvaluationReport;
import com.qcby.springboot_0525.mapper.EvaluationRecordMapper;
import com.qcby.springboot_0525.mapper.EvaluationReportMapper;
import com.qcby.springboot_0525.mapper.TrainingResultMapper;
import com.qcby.springboot_0525.entity.TrainingResult;
import com.qcby.springboot_0525.service.ReportService;
import com.qcby.springboot_0525.util.ExcelUtil;
import com.qcby.springboot_0525.util.PdfUtil;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.math.BigDecimal;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 报表中心控制器（报告生成、Excel/PDF导出）
 */
@RestController
@RequestMapping("/report")
public class ReportController {

    @Resource
    private EvaluationReportMapper reportMapper;

    @Resource
    private EvaluationRecordMapper recordMapper;
    
    @Resource
    private TrainingResultMapper resultMapper;

    @Resource
    private ReportService reportService;

    @Resource
    private ExcelUtil excelUtil;

    @Resource
    private PdfUtil pdfUtil;

    /**
     * 获取单个报告详情(含reportData)
     */
    @GetMapping("/{id}")
    public Result<EvaluationReport> getReportDetail(@PathVariable Long id) {
        EvaluationReport report = reportMapper.selectById(id);
        if (report == null) {
            throw new BusinessException("报告不存在");
        }
        return Result.success(report);
    }

    /**
     * 生成单个学生的评价报告
     */
    @SaCheckRole("teacher")
    @PostMapping("/generate/{resultId}")
    public Result<EvaluationReport> generateReport(@PathVariable Long resultId) {
        EvaluationReport report = reportService.generateReport(resultId);
        return Result.success("报告生成成功", report);
    }

    /**
     * 通过任务ID和学生ID生成报告（提交到学生端）
     */
    @SaCheckRole("teacher")
    @PostMapping("/generate-by-task-student")
    public Result<EvaluationReport> generateReportByTaskStudent(@RequestBody Map<String, Object> params) {
        Long taskId = Long.valueOf(params.get("taskId").toString());
        Long studentId = Long.valueOf(params.get("studentId").toString());
        
        // 查找对应的成果
        TrainingResult result = resultMapper.selectByTaskAndStudent(taskId, studentId);
        if (result == null) {
            throw new BusinessException(404, "未找到对应的成果");
        }
        
        EvaluationReport report = reportService.generateReport(result.getId());
        return Result.success("报告已生成并提交给学生", report);
    }

    /**
     * 批量生成任务下所有学生的报告
     */
    @SaCheckRole("teacher")
    @PostMapping("/generate/task/{taskId}")
    public Result<Map<String, Object>> generateReportsByTask(@PathVariable Long taskId) {
        int successCount = reportService.generateReportsByTask(taskId);
        Map<String, Object> data = new HashMap<>();
        data.put("successCount", successCount);
        return Result.success("成功生成 " + successCount + " 份报告", data);
    }

    /**
     * 导出Excel格式评价报告
     */
    @SaCheckLogin
    @GetMapping("/export/excel/{reportId}")
    public void exportExcel(@PathVariable Long reportId, HttpServletResponse response) {
        EvaluationReport report = reportMapper.selectById(reportId);
        if (report == null) {
            throw new BusinessException("报告不存在");
        }

        try {
            response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            response.setHeader("Content-Disposition",
                    "attachment;filename=" + URLEncoder.encode("实训评价报告.xlsx", "UTF-8"));

            // 通过 Service 获取正确的 resultId
            Long resultId = reportService.getResultIdByReport(report);
            List<EvaluationRecord> records = recordMapper.selectByResultId(resultId);
            excelUtil.exportEvaluationReport(report, records, response.getOutputStream());
        } catch (Exception e) {
            throw new BusinessException("Excel导出失败：" + e.getMessage());
        }
    }

    /**
     * 导出PDF格式评价报告
     */
    @SaCheckLogin
    @GetMapping("/export/pdf/{reportId}")
    public void exportPdf(@PathVariable Long reportId, HttpServletResponse response) {
        EvaluationReport report = reportMapper.selectById(reportId);
        if (report == null) {
            throw new BusinessException("报告不存在");
        }

        try {
            response.setContentType("application/pdf");
            response.setHeader("Content-Disposition",
                    "attachment;filename=" + URLEncoder.encode("实训评价报告.pdf", "UTF-8"));

            // 通过 Service 获取正确的 resultId
            Long resultId = reportService.getResultIdByReport(report);
            List<EvaluationRecord> records = recordMapper.selectByResultId(resultId);
            pdfUtil.exportEvaluationReport(report, records, response.getOutputStream());
        } catch (Exception e) {
            throw new BusinessException("PDF导出失败：" + e.getMessage());
        }
    }

    /**
     * 获取报告列表(分页)
     */
    @GetMapping("/page")
    public Result<PageResult<EvaluationReport>> list(
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(required = false) Long studentId,
            @RequestParam(required = false) Long taskId) {
        
        // 根据角色过滤数据
        String role = (String) StpUtil.getSession().get("role");
        Object realIdObj = StpUtil.getSession().get("realId");
        
        List<EvaluationReport> list;
        if ("teacher".equals(role) && realIdObj != null) {
            Long teacherId = Long.valueOf(realIdObj.toString());
            list = reportMapper.selectByTeacherId(teacherId);
        } else {
            list = reportMapper.selectList();
        }
        
        // 应用筛选条件
        if (studentId != null) {
            list = list.stream().filter(r -> r.getStudentId().equals(studentId)).collect(Collectors.toList());
        }
        if (taskId != null) {
            list = list.stream().filter(r -> r.getTaskId().equals(taskId)).collect(Collectors.toList());
        }
        
        // 手动分页
        int total = list.size();
        int fromIndex = (pageNum - 1) * pageSize;
        int toIndex = Math.min(fromIndex + pageSize, total);
        
        List<EvaluationReport> pageList;
        if (fromIndex >= total) {
            pageList = new java.util.ArrayList<>();
        } else {
            pageList = list.subList(fromIndex, toIndex);
        }
        
        return Result.success(PageResult.of(pageList, total, pageNum, pageSize));
    }

    /**
     * 教师对报告进行整体评分并设置AI/教师评分占比
     */
    @PutMapping("/teacher-score")
    public Result<Void> teacherScore(@RequestBody Map<String, Object> params) {
        String role = (String) StpUtil.getSession().get("role");
        if (!"teacher".equals(role)) {
            throw new BusinessException(403, "仅教师可评分");
        }
        
        Long reportId = Long.valueOf(params.get("reportId").toString());
        BigDecimal teacherScore = new BigDecimal(params.get("teacherScore").toString());
        BigDecimal ratio = new BigDecimal(params.get("ratio").toString()); // AI占比 0-1
        
        EvaluationReport report = reportMapper.selectById(reportId);
        if (report == null) {
            throw new BusinessException("报告不存在");
        }
        
        Long teacherId = Long.valueOf(StpUtil.getSession().get("realId").toString());
        report.setTeacherScore(teacherScore);
        report.setTeacherScoreRatio(ratio);
        report.setTeacherId(teacherId);
        reportMapper.updateById(report);
        
        return Result.success();
    }
    
    /**
     * 获取报告详情（含教师评分和最终展示分数）
     */
    @GetMapping("/detail/{id}")
    public Result<Map<String, Object>> getReportWithFinalScore(@PathVariable Long id) {
        EvaluationReport report = reportMapper.selectById(id);
        if (report == null) {
            throw new BusinessException("报告不存在");
        }
        
        Map<String, Object> data = new HashMap<>();
        data.put("report", report);
        
        // 计算最终展示分数
        if (report.getTeacherScore() != null && report.getTeacherScoreRatio() != null) {
            BigDecimal aiRatio = report.getTeacherScoreRatio(); // AI占比
            BigDecimal teacherRatio = BigDecimal.ONE.subtract(aiRatio); // 教师占比
            BigDecimal aiScore = report.getTotalScore() != null ? report.getTotalScore() : BigDecimal.ZERO;
            BigDecimal teacherScore = report.getTeacherScore();
            
            // 最终分 = AI分 × AI占比 + 教师分 × 教师占比
            BigDecimal finalScore = aiScore.multiply(aiRatio).add(teacherScore.multiply(teacherRatio));
            data.put("finalScore", finalScore.setScale(2, BigDecimal.ROUND_HALF_UP));
            data.put("aiScore", aiScore);
            data.put("teacherScore", teacherScore);
            data.put("aiRatio", aiRatio);
            data.put("teacherRatio", teacherRatio);
        } else {
            data.put("finalScore", report.getTotalScore());
            data.put("aiScore", report.getTotalScore());
        }
        
        return Result.success(data);
    }
    
    /**
     * 获取报告列表
     */
    @GetMapping("/list")
    public Result<List<EvaluationReport>> listAll() {
        return Result.success(reportMapper.selectList());
    }
    
    /**
     * 根据成果ID获取报告（用于报表中心显示教师评分和最终分数）
     */
    @GetMapping("/by-result")
    public Result<Map<String, Object>> getReportByResultId(@RequestParam Long resultId) {
        // 先通过resultId找到对应的training_result
        TrainingResult result = resultMapper.selectById(resultId);
        if (result == null) {
            return Result.success(null);
        }
        
        // 通过taskId和studentId查找报告
        List<EvaluationReport> reports = reportMapper.selectByTaskAndStudent(result.getTaskId(), result.getStudentId());
        if (reports == null || reports.isEmpty()) {
            return Result.success(null);
        }
        
        // 取最新的一份报告
        EvaluationReport report = reports.get(0);
        Map<String, Object> data = new HashMap<>();
        data.put("id", report.getId());
        data.put("teacherScore", report.getTeacherScore());
        data.put("teacherScoreRatio", report.getTeacherScoreRatio());
        
        // 计算最终分数
        if (report.getTeacherScore() != null && report.getTeacherScoreRatio() != null) {
            BigDecimal aiRatio = report.getTeacherScoreRatio();
            BigDecimal teacherRatio = BigDecimal.ONE.subtract(aiRatio);
            BigDecimal aiScore = report.getTotalScore() != null ? report.getTotalScore() : BigDecimal.ZERO;
            BigDecimal teacherScore = report.getTeacherScore();
            BigDecimal finalScore = aiScore.multiply(aiRatio).add(teacherScore.multiply(teacherRatio));
            data.put("finalScore", finalScore.setScale(2, BigDecimal.ROUND_HALF_UP));
        } else {
            // 没有教师评分时，最终分=AI分
            data.put("finalScore", report.getTotalScore());
        }
        
        return Result.success(data);
    }

    /**
     * 删除报告
     */
    @SaCheckRole("teacher")
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        reportService.deleteReport(id);
        return Result.success();
    }
}
