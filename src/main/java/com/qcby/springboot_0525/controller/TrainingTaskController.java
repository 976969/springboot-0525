package com.qcby.springboot_0525.controller;

import cn.dev33.satoken.stp.StpUtil;
import com.qcby.springboot_0525.common.BusinessException;
import com.qcby.springboot_0525.common.PageResult;
import com.qcby.springboot_0525.common.Result;
import com.qcby.springboot_0525.entity.TrainingTask;
import com.qcby.springboot_0525.service.TrainingTaskService;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

/**
 * 实训任务控制器（任务管理、过期状态判断、权限过滤）
 */
@RestController
@RequestMapping("/task")
public class TrainingTaskController {

    @Resource
    private TrainingTaskService taskService;

    @GetMapping("/page")
    public Result<PageResult<TrainingTask>> list(
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(required = false) String sortField,
            @RequestParam(required = false) String sortOrder) {
        
        // 根据角色过滤数据
        String role = (String) StpUtil.getSession().get("role");
        Object realIdObj = StpUtil.getSession().get("realId");
        
        List<TrainingTask> list;
        if ("teacher".equals(role) && realIdObj != null) {
            Long teacherId = Long.valueOf(realIdObj.toString());
            list = taskService.listByTeacherId(teacherId);
        } else if ("student".equals(role)) {
            list = taskService.listActive();
        } else {
            list = taskService.list();
        }
        
        // 动态判断过期状态
        Date now = new Date();
        for (TrainingTask task : list) {
            if (task.getDeadline() != null && task.getDeadline().before(now) && task.getStatus() == 1) {
                task.setStatus(3);
            }
        }
        
        // 应用排序
        if (sortField != null && !sortField.isEmpty() && sortOrder != null && !sortOrder.isEmpty()) {
            boolean asc = "asc".equalsIgnoreCase(sortOrder);
            Comparator<TrainingTask> comparator = null;
            
            if ("courseName".equals(sortField)) {
                Comparator<TrainingTask> comp = Comparator.comparing(
                    (TrainingTask t) -> t.getCourseName() != null ? t.getCourseName() : ""
                );
                comparator = asc ? comp : comp.reversed();
            } else if ("deadline".equals(sortField)) {
                comparator = asc
                    ? Comparator.comparing(TrainingTask::getDeadline, Comparator.nullsLast(Comparator.naturalOrder()))
                    : Comparator.comparing(TrainingTask::getDeadline, Comparator.nullsLast(Comparator.reverseOrder()));
            }
            
            if (comparator != null) {
                list.sort(comparator);
            }
        }
        
        // 手动分页
        int total = list.size();
        int fromIndex = (pageNum - 1) * pageSize;
        int toIndex = Math.min(fromIndex + pageSize, total);
        
        List<TrainingTask> pageList;
        if (fromIndex >= total) {
            pageList = new java.util.ArrayList<>();
        } else {
            pageList = list.subList(fromIndex, toIndex);
        }
        
        return Result.success(PageResult.of(pageList, total, pageNum, pageSize));
    }

    @GetMapping("/list")
    public Result<List<TrainingTask>> listAll() {
        // 学生只能看到未过期的任务
        String role = (String) StpUtil.getSession().get("role");
        if ("student".equals(role)) {
            return Result.success(taskService.listActive());
        }
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
        // 验证权限:只有教师和管理员可以发布任务
        String role = (String) StpUtil.getSession().get("role");
        if (!"teacher".equals(role) && !"admin".equals(role)) {
            throw new BusinessException(403, "无权限发布任务");
        }
        taskService.add(task);
        return Result.success();
    }

    @PutMapping
    public Result<Void> update(@RequestBody TrainingTask task) {
        // 验证权限:只有教师和管理员可以编辑任务
        String role = (String) StpUtil.getSession().get("role");
        if (!"teacher".equals(role) && !"admin".equals(role)) {
            throw new BusinessException(403, "无权限编辑任务");
        }
        taskService.update(task);
        return Result.success();
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        // 验证权限:只有教师和管理员可以删除任务
        String role = (String) StpUtil.getSession().get("role");
        if (!"teacher".equals(role) && !"admin".equals(role)) {
            throw new BusinessException(403, "无权限删除任务");
        }
        taskService.delete(id);
        return Result.success();
    }
}
