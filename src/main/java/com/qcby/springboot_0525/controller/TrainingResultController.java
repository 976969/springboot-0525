package com.qcby.springboot_0525.controller;

import cn.dev33.satoken.stp.StpUtil;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.qcby.springboot_0525.common.BusinessException;
import com.qcby.springboot_0525.common.PageResult;
import com.qcby.springboot_0525.common.Result;
import com.qcby.springboot_0525.entity.TrainingResult;
import com.qcby.springboot_0525.entity.TrainingTask;
import com.qcby.springboot_0525.service.TrainingResultService;
import com.qcby.springboot_0525.service.TrainingTaskService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 实训成果控制器（文件上传、覆盖提交、防重复）
 */
@RestController
@RequestMapping("/result")
public class TrainingResultController {

    private static final Logger log = LoggerFactory.getLogger(TrainingResultController.class);

    @Resource
    private TrainingResultService resultService;

    @Resource
    private TrainingTaskService taskService;

    @GetMapping("/page")
    public Result<PageResult<TrainingResult>> list(
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(required = false) Long studentId,
            @RequestParam(required = false) Long taskId,
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) String fileName) {
        
        // 根据角色过滤数据
        String role = (String) StpUtil.getSession().get("role");
        Object realIdObj = StpUtil.getSession().get("realId");
        
        log.info("查询成果列表 - 角色: {}, realId: {}, 筛选条件: studentId={}, taskId={}, status={}, fileName={}", 
                role, realIdObj, studentId, taskId, status, fileName);
        
        List<TrainingResult> list;
        if ("teacher".equals(role) && realIdObj != null) {
            // 教师只能看到自己课程相关的成果
            Long teacherId = Long.valueOf(realIdObj.toString());
            log.info("教师ID: {}", teacherId);
            list = resultService.listByTeacherId(teacherId);
            log.info("教师查询到 {} 条成果", list.size());
        } else if ("student".equals(role)) {
            // 学生只能看到自己的成果
            list = resultService.listByCurrentUser();
            log.info("学生查询到 {} 条成果", list.size());
        } else {
            // 管理员看到所有成果
            list = resultService.list();
            log.info("管理员查询到 {} 条成果", list.size());
        }
        
        // 应用筛选条件过滤(在分页之前)
        if (studentId != null) {
            list = list.stream().filter(r -> r.getStudentId().equals(studentId)).collect(Collectors.toList());
            log.info("应用学生筛选后: {} 条", list.size());
        }
        if (taskId != null) {
            list = list.stream().filter(r -> r.getTaskId().equals(taskId)).collect(Collectors.toList());
            log.info("应用任务筛选后: {} 条", list.size());
        }
        if (status != null) {
            list = list.stream().filter(r -> r.getStatus().equals(status)).collect(Collectors.toList());
            log.info("应用状态筛选后: {} 条", list.size());
        }
        if (fileName != null && !fileName.trim().isEmpty()) {
            list = list.stream().filter(r -> r.getFileName() != null && r.getFileName().toLowerCase().contains(fileName.toLowerCase())).collect(Collectors.toList());
            log.info("应用文件名筛选后: {} 条", list.size());
        }
        
        log.info("筛选完成后最终数据: {} 条", list.size());
        
        // 手动实现内存分页(PageHelper对内存List无效)
        int total = list.size();
        int fromIndex = (pageNum - 1) * pageSize;
        int toIndex = Math.min(fromIndex + pageSize, total);
        
        List<TrainingResult> pageList;
        if (fromIndex >= total) {
            pageList = new java.util.ArrayList<>();
        } else {
            pageList = list.subList(fromIndex, toIndex);
        }
        
        log.info("分页参数: pageNum={}, pageSize={}, fromIndex={}, toIndex={}, 返回数据: {} 条", 
                pageNum, pageSize, fromIndex, toIndex, pageList.size());
        
        return Result.success(PageResult.of(pageList, total, pageNum, pageSize));
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
                                          @RequestParam("taskId") Long taskId,
                                          @RequestParam(value = "overwrite", defaultValue = "false") boolean overwrite) {
        try {
            // 检查任务是否过期
            TrainingTask task = taskService.getById(taskId);
            if (task == null) {
                throw new BusinessException(404, "任务不存在");
            }
            if (task.getDeadline() != null && task.getDeadline().before(new Date())) {
                throw new BusinessException(403, "该任务已过期,无法提交");
            }
            
            TrainingResult result = resultService.upload(file, taskId, overwrite);
            return Result.success("上传成功", result);
        } catch (BusinessException e) {
            // 特殊处理"已提交"异常(409状态码)
            if (e.getCode() == 409) {
                return Result.fail(409, "该任务已提交过成果,是否覆盖提交?");
            }
            throw e;
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
