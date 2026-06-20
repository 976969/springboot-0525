package com.qcby.springboot_0525.controller;

import com.qcby.springboot_0525.common.Result;
import com.qcby.springboot_0525.mapper.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.*;

/**
 * 首页统计数据控制器
 */
@RestController
@RequestMapping("/dashboard")
public class DashboardController {

    @Autowired
    private CourseMapper courseMapper;

    @Autowired
    private TrainingTaskMapper taskMapper;

    @Autowired
    private TrainingResultMapper resultMapper;

    @Autowired
    private EvaluationReportMapper reportMapper;

    @Autowired
    private StudentMapper studentMapper;

    @Autowired
    private CourseStudentMapper courseStudentMapper;

    /**
     * 学生端首页统计数据
     */
    @GetMapping("/student/stats")
    public Result<Map<String, Object>> getStudentStats() {
        Map<String, Object> stats = new HashMap<>();
        
        // 1. 我的课程数 - 查询当前学生选了多少门课
        // TODO: 需要根据当前登录的学生ID查询
        stats.put("courseCount", 3); // 临时数据
        
        // 2. 已完成任务数
        stats.put("completedTaskCount", 8); // 临时数据
        
        // 3. 平均成绩
        stats.put("averageScore", 85.6); // 临时数据
        
        // 4. 获得证书数(评价报告数)
        stats.put("certificateCount", 5); // 临时数据
        
        return Result.success(stats);
    }

    /**
     * 学生端-学习进度趋势(最近30天)
     */
    @GetMapping("/student/progress")
    public Result<List<Map<String, Object>>> getStudentProgress() {
        List<Map<String, Object>> progressData = new ArrayList<>();
        
        // 生成最近30天的模拟数据
        for (int i = 29; i >= 0; i--) {
            Map<String, Object> data = new HashMap<>();
            // 日期: 从今天往前推i天
            Calendar cal = Calendar.getInstance();
            cal.add(Calendar.DAY_OF_MONTH, -i);
            data.put("date", String.format("%02d-%02d", 
                cal.get(Calendar.MONTH) + 1, 
                cal.get(Calendar.DAY_OF_MONTH)));
            
            // 学习时长: 随机60-180分钟
            data.put("studyTime", 60 + new Random().nextInt(120));
            
            progressData.add(data);
        }
        
        return Result.success(progressData);
    }

    /**
     * 学生端-课程完成度(饼图)
     */
    @GetMapping("/student/completion")
    public Result<List<Map<String, Object>>> getStudentCompletion() {
        List<Map<String, Object>> completionData = new ArrayList<>();
        
        // 5门课程的完成情况
        String[] courses = {"Java程序设计", "Web前端开发", "数据库原理", "软件工程", "移动应用开发"};
        int[] completed = {4, 3, 3, 2, 2};
        int[] total = {4, 3, 3, 2, 2};
        
        for (int i = 0; i < courses.length; i++) {
            Map<String, Object> data = new HashMap<>();
            data.put("name", courses[i]);
            data.put("value", (int)((double)completed[i] / total[i] * 100));
            completionData.add(data);
        }
        
        return Result.success(completionData);
    }

    /**
     * 学生端-成绩分布(柱状图)
     */
    @GetMapping("/student/scores")
    public Result<Map<String, Object>> getStudentScores() {
        Map<String, Object> scoreData = new HashMap<>();
        
        // 成绩区间分布
        scoreData.put("ranges", Arrays.asList("90-100", "80-89", "70-79", "60-69", "<60"));
        scoreData.put("counts", Arrays.asList(5, 8, 3, 0, 0));
        
        return Result.success(scoreData);
    }

    /**
     * 教师端首页统计数据
     */
    @GetMapping("/teacher/stats")
    public Result<Map<String, Object>> getTeacherStats() {
        Map<String, Object> stats = new HashMap<>();
        
        // 1. 教授课程数 (查询所有课程)
        stats.put("courseCount", 5); // 根据实际数据库
        
        // 2. 学生总数
        stats.put("studentCount", 10); // 根据实际数据库
        
        // 3. 待批改任务数
        // status=0 表示待核查
        stats.put("pendingTasks", 5); // 临时数据
        
        // 4. 平均成绩
        stats.put("averageScore", 85.2); // 临时数据
        
        return Result.success(stats);
    }

    /**
     * 教师端-各课程学生人数(柱状图)
     */
    @GetMapping("/teacher/students")
    public Result<List<Map<String, Object>>> getTeacherStudentStats() {
        List<Map<String, Object>> studentData = new ArrayList<>();
        
        // 查询每门课程的学生人数
        // 这里用模拟数据,实际应该查数据库
        String[] courses = {"Java程序设计", "Web前端开发", "数据库原理", "软件工程", "移动应用开发"};
        int[] counts = {3, 2, 5, 5, 6};
        
        for (int i = 0; i < courses.length; i++) {
            Map<String, Object> data = new HashMap<>();
            data.put("course", courses[i]);
            data.put("students", counts[i]);
            studentData.add(data);
        }
        
        return Result.success(studentData);
    }

    /**
     * 教师端-任务提交趋势(近7天)
     */
    @GetMapping("/teacher/tasks")
    public Result<List<Map<String, Object>>> getTeacherTaskStats() {
        List<Map<String, Object>> taskData = new ArrayList<>();
        
        // 生成最近7天的数据
        for (int i = 6; i >= 0; i--) {
            Map<String, Object> data = new HashMap<>();
            Calendar cal = Calendar.getInstance();
            cal.add(Calendar.DAY_OF_MONTH, -i);
            data.put("date", String.format("%02d-%02d", 
                cal.get(Calendar.MONTH) + 1, 
                cal.get(Calendar.DAY_OF_MONTH)));
            
            // 提交数量: 随机5-20
            data.put("submissions", 5 + new Random().nextInt(15));
            
            taskData.add(data);
        }
        
        return Result.success(taskData);
    }

    /**
     * 教师端-成绩分布(饼图)
     */
    @GetMapping("/teacher/scores")
    public Result<List<Map<String, Object>>> getTeacherScoreStats() {
        List<Map<String, Object>> scoreData = new ArrayList<>();
        
        // 成绩分布
        Map<String, Object> excellent = new HashMap<>();
        excellent.put("name", "优秀(90-100)");
        excellent.put("value", 8);
        scoreData.add(excellent);
        
        Map<String, Object> good = new HashMap<>();
        good.put("name", "良好(80-89)");
        good.put("value", 15);
        scoreData.add(good);
        
        Map<String, Object> medium = new HashMap<>();
        medium.put("name", "中等(70-79)");
        medium.put("value", 5);
        scoreData.add(medium);
        
        Map<String, Object> pass = new HashMap<>();
        pass.put("name", "及格(60-69)");
        pass.put("value", 2);
        scoreData.add(pass);
        
        Map<String, Object> fail = new HashMap<>();
        fail.put("name", "不及格(<60)");
        fail.put("value", 0);
        scoreData.add(fail);
        
        return Result.success(scoreData);
    }
}
