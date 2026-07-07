package com.qcby.springboot_0525.controller;

import cn.dev33.satoken.stp.StpUtil;
import com.qcby.springboot_0525.common.BusinessException;
import com.qcby.springboot_0525.common.Result;
import com.qcby.springboot_0525.entity.ClassSchedule;
import com.qcby.springboot_0525.entity.Course;
import com.qcby.springboot_0525.mapper.CourseMapper;
import com.qcby.springboot_0525.service.ClassScheduleService;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

/**
 * 课程安排控制器（课表管理、权限过滤）
 */
@RestController
@RequestMapping("/schedule")
public class ClassScheduleController {

    @Resource
    private ClassScheduleService scheduleService;

    @Resource
    private CourseMapper courseMapper;

    @GetMapping("/list")
    public Result<List<ClassSchedule>> list(@RequestParam(required = false) Long teacherId) {
        String role = (String) StpUtil.getSession().get("role");
        Object realIdObj = StpUtil.getSession().get("realId");

        List<ClassSchedule> list;
        if ("teacher".equals(role) && realIdObj != null) {
            Long teacherIdSession = Long.valueOf(realIdObj.toString());
            list = scheduleService.listByTeacherId(teacherIdSession);
        } else if ("student".equals(role) && realIdObj != null) {
            Long studentId = Long.valueOf(realIdObj.toString());
            list = scheduleService.listByStudentId(studentId);
        } else if ("admin".equals(role) && teacherId != null) {
            // 管理员可按教师筛选
            list = scheduleService.listByTeacherId(teacherId);
        } else {
            list = scheduleService.listAll();
        }
        return Result.success(list);
    }

    /**
     * 管理员专用：根据角色和用户ID查询课程表
     * @param role teacher 或 student
     * @param userId 教师ID或学生ID
     */
    @GetMapping("/user")
    public Result<List<ClassSchedule>> listByUser(@RequestParam String role, @RequestParam Long userId) {
        String adminRole = (String) StpUtil.getSession().get("role");
        if (!"admin".equals(adminRole)) {
            throw new BusinessException(403, "无权限访问");
        }
        List<ClassSchedule> list;
        if ("teacher".equals(role)) {
            list = scheduleService.listByTeacherId(userId);
        } else if ("student".equals(role)) {
            list = scheduleService.listByStudentId(userId);
        } else {
            list = scheduleService.listAll();
        }
        return Result.success(list);
    }

    @GetMapping("/{id}")
    public Result<ClassSchedule> getById(@PathVariable Long id) {
        return Result.success(scheduleService.getById(id));
    }

    @PostMapping
    public Result<Void> add(@RequestBody ClassSchedule schedule) {
        String role = (String) StpUtil.getSession().get("role");
        if (!"teacher".equals(role) && !"admin".equals(role)) {
            throw new BusinessException(403, "无权限添加课程表");
        }

        // 教师角色自动填充 teacherId
        Object realIdObj = StpUtil.getSession().get("realId");
        if ("teacher".equals(role) && realIdObj != null) {
            schedule.setTeacherId(Long.valueOf(realIdObj.toString()));
        }

        // 管理员或教师：根据课程自动填充 teacherId（以课程的负责教师为准）
        if (schedule.getCourseId() != null) {
            Course course = courseMapper.selectById(schedule.getCourseId());
            if (course == null) {
                throw new BusinessException(400, "课程不存在");
            }
            schedule.setTeacherId(course.getTeacherId());
        }

        scheduleService.add(schedule);
        return Result.success();
    }

    @PutMapping
    public Result<Void> update(@RequestBody ClassSchedule schedule) {
        String role = (String) StpUtil.getSession().get("role");
        if (!"teacher".equals(role) && !"admin".equals(role)) {
            throw new BusinessException(403, "无权限修改课程表");
        }
        scheduleService.update(schedule);
        return Result.success();
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        String role = (String) StpUtil.getSession().get("role");
        if (!"teacher".equals(role) && !"admin".equals(role)) {
            throw new BusinessException(403, "无权限删除课程表");
        }
        scheduleService.delete(id);
        return Result.success();
    }
}
