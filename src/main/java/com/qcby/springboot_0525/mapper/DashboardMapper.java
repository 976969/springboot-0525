package com.qcby.springboot_0525.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

/**
 * 首页统计数据数据访问层（Dashboard专用）
 */
@Mapper
public interface DashboardMapper {

    // ============ 学生端 ============

    /** 学生选课数量 */
    int countStudentCourses(@Param("studentId") Long studentId);

    /** 学生已提交任务数 */
    int countStudentCompletedTasks(@Param("studentId") Long studentId);

    /** 学生平均成绩（final_score） */
    Double getStudentAverageScore(@Param("studentId") Long studentId);

    /** 学生获得评价报告数（视为证书/成果认证） */
    int countStudentReports(@Param("studentId") Long studentId);

    /** 学生待评价成果数（status=0） */
    int countStudentPendingResults(@Param("studentId") Long studentId);

    /** 学生最近学习天数（从首次提交到现在的天数） */
    int getStudentStudyDays(@Param("studentId") Long studentId);

    /** 学生最近30天每日提交数量（学习进度） */
    List<Map<String, Object>> getStudentProgress30Days(@Param("studentId") Long studentId);

    /** 学生各课程完成度（已完成任务数/总任务数） */
    List<Map<String, Object>> getStudentCourseCompletion(@Param("studentId") Long studentId);

    /** 学生成绩分布（按分数段统计） */
    List<Map<String, Object>> getStudentScoreDistribution(@Param("studentId") Long studentId);

    // ============ 教师端 ============

    /** 教师教授课程数 */
    int countTeacherCourses(@Param("teacherId") Long teacherId);

    /** 教师学生总数（去重） */
    int countTeacherStudents(@Param("teacherId") Long teacherId);

    /** 教师待批改任务数（成果status=0） */
    int countTeacherPendingTasks(@Param("teacherId") Long teacherId);

    /** 教师学生平均成绩 */
    Double getTeacherAverageScore(@Param("teacherId") Long teacherId);

    /** 教师各课程学生人数 */
    List<Map<String, Object>> getTeacherCourseStudents(@Param("teacherId") Long teacherId);

    /** 教师近7天每日提交数量 */
    List<Map<String, Object>> getTeacherTaskTrend7Days(@Param("teacherId") Long teacherId);

    /** 教师学生成绩分布（按分数段统计） */
    List<Map<String, Object>> getTeacherScoreDistribution(@Param("teacherId") Long teacherId);

    // ============ 管理员端 ============

    /** 学生总数 */
    int countAllStudents();

    /** 教师总数 */
    int countAllTeachers();

    /** 课程总数 */
    int countAllCourses();

    /** 实训任务总数 */
    int countAllTasks();

    /** 待评价成果数（status=0） */
    int countPendingResults();

    /** 近30天每日成果提交量 */
    List<Map<String, Object>> getSystemProgress30Days();

    /** 各课程选课人数 */
    List<Map<String, Object>> getCourseEnrollment();

    /** 全系统成绩分布（按分数段） */
    List<Map<String, Object>> getSystemScoreDistribution();

    /** 成果状态分布（待评价/已评价） */
    List<Map<String, Object>> getResultStatusDistribution();

    /** 最近10条提交记录 */
    List<Map<String, Object>> getRecentSubmissions();

    // ============ 管理员评价/报表统计 ============

    /** AI评分总览统计(总成果数、已评分数、待评分数、平均AI分) */
    Map<String, Object> getAdminEvaluationStats();

    /** 按任务分组的评分摘要 */
    List<Map<String, Object>> getEvaluationByTask();

    /** 按教师分组的评分摘要 */
    List<Map<String, Object>> getEvaluationByTeacher();

    /** 报表总览统计(总报告数、平均最终分、教师已评分率) */
    Map<String, Object> getAdminReportStats();

    /** 各任务报告平均分对比 */
    List<Map<String, Object>> getReportScoreByTask();

    /** 各教师报告平均分对比 */
    List<Map<String, Object>> getReportScoreByTeacher();

    /** 按任务维度的组合统计（AI评分 + 报告评分） */
    List<Map<String, Object>> getCombinedTaskStats();
}
