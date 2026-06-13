package com.qcby.springboot_0525.controller;

import cn.dev33.satoken.stp.StpUtil;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.qcby.springboot_0525.common.PageResult;
import com.qcby.springboot_0525.common.Result;
import com.qcby.springboot_0525.entity.Course;
import com.qcby.springboot_0525.service.CourseService;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

@RestController
@RequestMapping("/course")
public class CourseController {

    @Resource
    private CourseService courseService;

    @GetMapping("/page")
    public Result<PageResult<Course>> list(
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize) {
        PageHelper.startPage(pageNum, pageSize);
        
        // 根据角色过滤数据
        String role = (String) StpUtil.getSession().get("role");
        Object realIdObj = StpUtil.getSession().get("realId");
        
        List<Course> list;
        if ("teacher".equals(role) && realIdObj != null) {
            // 教师只能看到自己的课程
            Long teacherId = Long.valueOf(realIdObj.toString());
            list = courseService.listByTeacherId(teacherId);
        } else {
            // 管理员看到所有课程
            list = courseService.list();
        }
        
        PageInfo<Course> pageInfo = new PageInfo<>(list);
        return Result.success(PageResult.of(pageInfo.getList(), pageInfo.getTotal(), pageNum, pageSize));
    }

    @GetMapping("/list")
    public Result<List<Course>> listAll() {
        return Result.success(courseService.list());
    }

    @GetMapping("/{id}")
    public Result<Course> getById(@PathVariable Long id) {
        return Result.success(courseService.getById(id));
    }

    @PostMapping
    public Result<Void> add(@RequestBody Course course) {
        courseService.add(course);
        return Result.success();
    }

    @PutMapping
    public Result<Void> update(@RequestBody Course course) {
        courseService.update(course);
        return Result.success();
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        courseService.delete(id);
        return Result.success();
    }
}
