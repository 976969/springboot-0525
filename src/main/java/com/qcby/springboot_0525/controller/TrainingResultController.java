package com.qcby.springboot_0525.controller;

import cn.dev33.satoken.stp.StpUtil;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.qcby.springboot_0525.common.PageResult;
import com.qcby.springboot_0525.common.Result;
import com.qcby.springboot_0525.entity.TrainingResult;
import com.qcby.springboot_0525.service.TrainingResultService;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.util.List;

@RestController
@RequestMapping("/result")
public class TrainingResultController {

    @Resource
    private TrainingResultService resultService;

    @GetMapping("/page")
    public Result<PageResult<TrainingResult>> list(
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize) {
        PageHelper.startPage(pageNum, pageSize);
        
        // 根据角色过滤数据
        String role = (String) StpUtil.getSession().get("role");
        Object realIdObj = StpUtil.getSession().get("realId");
        
        List<TrainingResult> list;
        if ("teacher".equals(role) && realIdObj != null) {
            // 教师只能看到自己课程相关的成果
            Long teacherId = Long.valueOf(realIdObj.toString());
            list = resultService.listByTeacherId(teacherId);
        } else {
            // 管理员看到所有成果
            list = resultService.list();
        }
        
        PageInfo<TrainingResult> pageInfo = new PageInfo<>(list);
        return Result.success(PageResult.of(pageInfo.getList(), pageInfo.getTotal(), pageNum, pageSize));
    }

    @GetMapping("/list")
    public Result<List<TrainingResult>> listAll() {
        return Result.success(resultService.list());
    }

    @GetMapping("/{id}")
    public Result<TrainingResult> getById(@PathVariable Long id) {
        return Result.success(resultService.getById(id));
    }

    @GetMapping("/task/{taskId}")
    public Result<List<TrainingResult>> listByTask(@PathVariable Long taskId) {
        return Result.success(resultService.listByTaskId(taskId));
    }

    @GetMapping("/student/{studentId}")
    public Result<List<TrainingResult>> listByStudent(@PathVariable Long studentId) {
        return Result.success(resultService.listByStudentId(studentId));
    }

    /**
     * 查询当前登录学生自己的成果
     */
    @GetMapping("/my")
    public Result<List<TrainingResult>> listMyResults() {
        return Result.success(resultService.listByCurrentUser());
    }

    /**
     * 查询当前登录学生自己的成果（分页）
     */
    @GetMapping("/my/page")
    public Result<PageResult<TrainingResult>> listMyResultsPage(
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize) {
        PageHelper.startPage(pageNum, pageSize);
        List<TrainingResult> list = resultService.listByCurrentUser();
        PageInfo<TrainingResult> pageInfo = new PageInfo<>(list);
        return Result.success(PageResult.of(pageInfo.getList(), pageInfo.getTotal(), pageNum, pageSize));
    }

    /**
     * 上传实训成果文件
     */
    @PostMapping("/upload")
    public Result<TrainingResult> upload(@RequestParam("file") MultipartFile file,
                                          @RequestParam("taskId") Long taskId) {
        try {
            TrainingResult result = resultService.upload(file, taskId);
            return Result.success("上传成功", result);
        } catch (Exception e) {
            return Result.fail("上传失败：" + e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        resultService.delete(id);
        return Result.success();
    }
}
