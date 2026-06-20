package com.qcby.springboot_0525.controller;

import cn.dev33.satoken.stp.StpUtil;
import com.qcby.springboot_0525.common.BusinessException;
import com.qcby.springboot_0525.common.Result;
import com.qcby.springboot_0525.entity.ClassSchedule;
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

    @GetMapping("/list")
    public Result<List<ClassSchedule>> list() {
        String role = (String) StpUtil.getSession().get("role");
        Object realIdObj = StpUtil.getSession().get("realId");

        List<ClassSchedule> list;
        if ("teacher".equals(role) && realIdObj != null) {
            Long teacherId = Long.valueOf(realIdObj.toString());
            list = scheduleService.listByTeacherId(teacherId);
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
