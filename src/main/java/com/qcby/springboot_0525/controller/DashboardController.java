package com.qcby.springboot_0525.controller;

import cn.dev33.satoken.stp.StpUtil;
import com.qcby.springboot_0525.common.Result;
import com.qcby.springboot_0525.mapper.DashboardMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.*;

/**
 * 首页统计数据控制器（真实数据库查询）
 */
@RestController
@RequestMapping("/dashboard")
public class DashboardController {

    @Autowired
    private DashboardMapper dashboardMapper;

    /**
     * 从 Sa-Token Session 中获取当前登录用户的真实ID
     */
    private Long getCurrentUserId() {
        Object realIdObj = StpUtil.getSession().get("realId");
        if (realIdObj == null) {
            return null;
        }
        if (realIdObj instanceof Long) {
            return (Long) realIdObj;
        }
        return Long.valueOf(realIdObj.toString());
    }

    /**
     * 学生端首页统计数据
     */
    @GetMapping("/student/stats")
    public Result<Map<String, Object>> getStudentStats() {
        Long studentId = getCurrentUserId();
        if (studentId == null) {
            return Result.fail(401, "未登录");
        }

        Map<String, Object> stats = new HashMap<>();
        stats.put("courseCount", dashboardMapper.countStudentCourses(studentId));
        stats.put("completedTaskCount", dashboardMapper.countStudentCompletedTasks(studentId));
        stats.put("averageScore", dashboardMapper.getStudentAverageScore(studentId));
        stats.put("pendingResults", dashboardMapper.countStudentPendingResults(studentId));

        return Result.success(stats);
    }

    /**
     * 学生端-学习天数（从首次提交算起）
     */
    @GetMapping("/student/study-days")
    public Result<Map<String, Object>> getStudentStudyDays() {
        Long studentId = getCurrentUserId();
        if (studentId == null) {
            return Result.fail(401, "未登录");
        }
        int days = dashboardMapper.getStudentStudyDays(studentId);
        Map<String, Object> data = new HashMap<>();
        data.put("studyDays", days > 0 ? days : 1);
        return Result.success(data);
    }

    /**
     * 学生端-提交记录趋势(最近30天)
     */
    @GetMapping("/student/progress")
    public Result<List<Map<String, Object>>> getStudentProgress() {
        Long studentId = getCurrentUserId();
        if (studentId == null) {
            return Result.success(new ArrayList<>());
        }
        List<Map<String, Object>> progressData = dashboardMapper.getStudentProgress30Days(studentId);
        return Result.success(progressData);
    }

    /**
     * 学生端-课程完成度(饼图)
     */
    @GetMapping("/student/completion")
    public Result<List<Map<String, Object>>> getStudentCompletion() {
        Long studentId = getCurrentUserId();
        if (studentId == null) {
            return Result.success(new ArrayList<>());
        }
        List<Map<String, Object>> completionData = dashboardMapper.getStudentCourseCompletion(studentId);
        return Result.success(completionData);
    }

    /**
     * 学生端-成绩分布(柱状图)
     */
    @GetMapping("/student/scores")
    public Result<Map<String, Object>> getStudentScores() {
        Long studentId = getCurrentUserId();
        if (studentId == null) {
            Map<String, Object> empty = new HashMap<>();
            empty.put("ranges", new ArrayList<>());
            empty.put("counts", new ArrayList<>());
            return Result.success(empty);
        }
        List<Map<String, Object>> distribution = dashboardMapper.getStudentScoreDistribution(studentId);

        // 转换为前端需要的格式：ranges 和 counts
        Map<String, Object> scoreData = new HashMap<>();
        List<String> ranges = new ArrayList<>();
        List<Integer> counts = new ArrayList<>();
        for (Map<String, Object> item : distribution) {
            ranges.add(String.valueOf(item.get("name")));
            counts.add(((Number) item.get("value")).intValue());
        }
        scoreData.put("ranges", ranges);
        scoreData.put("counts", counts);

        return Result.success(scoreData);
    }

    /**
     * 教师端首页统计数据
     */
    @GetMapping("/teacher/stats")
    public Result<Map<String, Object>> getTeacherStats() {
        Long teacherId = getCurrentUserId();
        if (teacherId == null) {
            return Result.fail(401, "未登录");
        }

        Map<String, Object> stats = new HashMap<>();
        stats.put("courseCount", dashboardMapper.countTeacherCourses(teacherId));
        stats.put("studentCount", dashboardMapper.countTeacherStudents(teacherId));
        stats.put("pendingTasks", dashboardMapper.countTeacherPendingTasks(teacherId));
        stats.put("averageScore", dashboardMapper.getTeacherAverageScore(teacherId));

        return Result.success(stats);
    }

    /**
     * 教师端-各课程学生人数(柱状图)
     */
    @GetMapping("/teacher/students")
    public Result<List<Map<String, Object>>> getTeacherStudentStats() {
        Long teacherId = getCurrentUserId();
        if (teacherId == null) {
            return Result.success(new ArrayList<>());
        }
        List<Map<String, Object>> studentData = dashboardMapper.getTeacherCourseStudents(teacherId);
        return Result.success(studentData);
    }

    /**
     * 教师端-任务提交趋势(近7天)
     */
    @GetMapping("/teacher/tasks")
    public Result<List<Map<String, Object>>> getTeacherTaskStats() {
        Long teacherId = getCurrentUserId();
        if (teacherId == null) {
            return Result.success(new ArrayList<>());
        }
        List<Map<String, Object>> taskData = dashboardMapper.getTeacherTaskTrend7Days(teacherId);
        return Result.success(taskData);
    }

    /**
     * 教师端-成绩分布(饼图)
     */
    @GetMapping("/teacher/scores")
    public Result<List<Map<String, Object>>> getTeacherScoreStats() {
        Long teacherId = getCurrentUserId();
        if (teacherId == null) {
            return Result.success(new ArrayList<>());
        }
        List<Map<String, Object>> scoreData = dashboardMapper.getTeacherScoreDistribution(teacherId);
        return Result.success(scoreData);
    }

    // ============ 管理员端 ============

    /**
     * 管理员端首页统计数据
     */
    @GetMapping("/admin/stats")
    public Result<Map<String, Object>> getAdminStats() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("studentCount", dashboardMapper.countAllStudents());
        stats.put("teacherCount", dashboardMapper.countAllTeachers());
        stats.put("courseCount", dashboardMapper.countAllCourses());
        stats.put("taskCount", dashboardMapper.countAllTasks());
        stats.put("pendingResults", dashboardMapper.countPendingResults());
        return Result.success(stats);
    }

    /**
     * 管理员-近30天提交趋势
     */
    @GetMapping("/admin/progress")
    public Result<List<Map<String, Object>>> getAdminProgress() {
        return Result.success(dashboardMapper.getSystemProgress30Days());
    }

    /**
     * 管理员-各课程选课人数
     */
    @GetMapping("/admin/enrollment")
    public Result<List<Map<String, Object>>> getAdminEnrollment() {
        return Result.success(dashboardMapper.getCourseEnrollment());
    }

    /**
     * 管理员-成绩分布
     */
    @GetMapping("/admin/scores")
    public Result<Map<String, Object>> getAdminScores() {
        List<Map<String, Object>> distribution = dashboardMapper.getSystemScoreDistribution();
        Map<String, Object> scoreData = new HashMap<>();
        List<String> ranges = new ArrayList<>();
        List<Integer> counts = new ArrayList<>();
        for (Map<String, Object> item : distribution) {
            ranges.add(String.valueOf(item.get("name")));
            counts.add(((Number) item.get("value")).intValue());
        }
        scoreData.put("ranges", ranges);
        scoreData.put("counts", counts);
        return Result.success(scoreData);
    }

    /**
     * 管理员-成果状态分布
     */
    @GetMapping("/admin/result-status")
    public Result<List<Map<String, Object>>> getResultStatus() {
        return Result.success(dashboardMapper.getResultStatusDistribution());
    }

    /**
     * 管理员-最近提交记录
     */
    @GetMapping("/admin/recent")
    public Result<List<Map<String, Object>>> getRecentSubmissions() {
        return Result.success(dashboardMapper.getRecentSubmissions());
    }

    // ============ 管理员评价/报表统计 ============

    /**
     * 管理员-AI评分总览统计
     */
    @GetMapping("/admin/evaluation/stats")
    public Result<Map<String, Object>> getAdminEvaluationStats() {
        return Result.success(dashboardMapper.getAdminEvaluationStats());
    }

    /**
     * 管理员-按任务评分摘要
     */
    @GetMapping("/admin/evaluation/by-task")
    public Result<List<Map<String, Object>>> getEvaluationByTask() {
        return Result.success(dashboardMapper.getEvaluationByTask());
    }

    /**
     * 管理员-按教师评分摘要
     */
    @GetMapping("/admin/evaluation/by-teacher")
    public Result<List<Map<String, Object>>> getEvaluationByTeacher() {
        return Result.success(dashboardMapper.getEvaluationByTeacher());
    }

    /**
     * 管理员-报表总览统计
     */
    @GetMapping("/admin/report/stats")
    public Result<Map<String, Object>> getAdminReportStats() {
        return Result.success(dashboardMapper.getAdminReportStats());
    }

    /**
     * 管理员-各任务报告平均分对比
     */
    @GetMapping("/admin/report/by-task")
    public Result<List<Map<String, Object>>> getReportScoreByTask() {
        return Result.success(dashboardMapper.getReportScoreByTask());
    }

    /**
     * 管理员-各教师报告平均分对比
     */
    @GetMapping("/admin/report/by-teacher")
    public Result<List<Map<String, Object>>> getReportScoreByTeacher() {
        return Result.success(dashboardMapper.getReportScoreByTeacher());
    }

    /**
     * 管理员-按任务组合统计（AI评分+报告）
     */
    @GetMapping("/admin/combined/task-stats")
    public Result<List<Map<String, Object>>> getCombinedTaskStats() {
        return Result.success(dashboardMapper.getCombinedTaskStats());
    }
}
