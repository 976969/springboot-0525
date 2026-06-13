package com.qcby.springboot_0525.controller;

import cn.dev33.satoken.stp.StpUtil;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.qcby.springboot_0525.common.PageResult;
import com.qcby.springboot_0525.common.Result;
import com.qcby.springboot_0525.entity.TrainingTask;
import com.qcby.springboot_0525.service.TrainingTaskService;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

@RestController
@RequestMapping("/task")
public class TrainingTaskController {

    @Resource
    private TrainingTaskService taskService;

    @GetMapping("/page")
    public Result<PageResult<TrainingTask>> list(
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize) {
        PageHelper.startPage(pageNum, pageSize);
        
        // 根据角色过滤数据
        String role = (String) StpUtil.getSession().get("role");
        Object realIdObj = StpUtil.getSession().get("realId");
        
        List<TrainingTask> list;
        if ("teacher".equals(role) && realIdObj != null) {
            // 教师只能看到自己课程的任务
            Long teacherId = Long.valueOf(realIdObj.toString());
            list = taskService.listByTeacherId(teacherId);
        } else {
            // 管理员看到所有任务
            list = taskService.list();
        }
        
        PageInfo<TrainingTask> pageInfo = new PageInfo<>(list);
        return Result.success(PageResult.of(pageInfo.getList(), pageInfo.getTotal(), pageNum, pageSize));
    }

    @GetMapping("/list")
    public Result<List<TrainingTask>> listAll() {
        return Result.success(taskService.list());
    }

    @GetMapping("/{id}")
    public Result<TrainingTask> getById(@PathVariable Long id) {
        return Result.success(taskService.getById(id));
    }

    @GetMapping("/course/{courseId}")
    public Result<List<TrainingTask>> listByCourse(@PathVariable Long courseId) {
        return Result.success(taskService.listByCourseId(courseId));
    }

    @PostMapping
    public Result<Void> add(@RequestBody TrainingTask task) {
        taskService.add(task);
        return Result.success();
    }

    @PutMapping
    public Result<Void> update(@RequestBody TrainingTask task) {
        taskService.update(task);
        return Result.success();
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        taskService.delete(id);
        return Result.success();
    }
}
