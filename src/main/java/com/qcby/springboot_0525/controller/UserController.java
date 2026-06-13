package com.qcby.springboot_0525.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import cn.dev33.satoken.annotation.SaCheckRole;
import cn.dev33.satoken.stp.StpUtil;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.qcby.springboot_0525.common.BusinessException;
import com.qcby.springboot_0525.common.PageResult;
import com.qcby.springboot_0525.common.Result;
import com.qcby.springboot_0525.entity.Admin;
import com.qcby.springboot_0525.entity.CourseStudent;
import com.qcby.springboot_0525.entity.Student;
import com.qcby.springboot_0525.entity.Teacher;
import com.qcby.springboot_0525.service.*;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 用户管理控制器（三表分离版）
 * - 管理员: CRUD所有角色用户
 * - 教师/学生: 个人中心 + 课程学生管理
 */
@RestController
@RequestMapping("/user")
@SaCheckLogin
public class UserController {

    @Resource
    private AdminService adminService;

    @Resource
    private TeacherService teacherService;

    @Resource
    private StudentService studentService;

    /**
     * 从Session中获取真实用户ID（安全类型转换）
     */
    private Long getRealIdFromSession() {
        Object realIdObj = StpUtil.getSession().get("realId");
        
        if (realIdObj == null) {
            return null;
        }
        
        if (realIdObj instanceof Long) {
            return (Long) realIdObj;
        } else if (realIdObj instanceof Integer) {
            return ((Integer) realIdObj).longValue();
        } else if (realIdObj instanceof String) {
            return Long.parseLong((String) realIdObj);
        }
        
        return null;
    }

    @Resource
    private AuthService authService;

    @Resource
    private CourseStudentService courseStudentService;

    // ==================== 管理员功能 ====================

    /**
     * 获取所有用户列表（管理员）- 返回三表合并数据
     */
    @GetMapping("/list")
    @SaCheckRole("admin")
    public Result<Map<String, Object>> list() {
        Map<String, Object> data = new HashMap<>();
        data.put("adminList", adminService.list());
        data.put("teacherList", teacherService.list());
        data.put("studentList", studentService.list());
        return Result.success(data);
    }

    /**
     * 获取管理员列表（分页）
     */
    @GetMapping("/admin/page")
    @SaCheckRole("admin")
    public Result<PageResult<Admin>> listAdminPage(
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize) {
        PageHelper.startPage(pageNum, pageSize);
        List<Admin> list = adminService.list();
        PageInfo<Admin> pageInfo = new PageInfo<>(list);
        return Result.success(PageResult.of(pageInfo.getList(), pageInfo.getTotal(), pageNum, pageSize));
    }

    /**
     * 获取管理员列表
     */
    @GetMapping("/admin/list")
    @SaCheckRole("admin")
    public Result<List<Admin>> listAdmin() {
        return Result.success(adminService.list());
    }

    /**
     * 获取教师列表（分页）
     */
    @GetMapping("/teacher/page")
    public Result<PageResult<Teacher>> listTeacherPage(
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize) {
        String role = (String) StpUtil.getSession().get("role");
        if (!"admin".equals(role) && !"teacher".equals(role)) {
            throw new BusinessException("无权限");
        }
        PageHelper.startPage(pageNum, pageSize);
        List<Teacher> list = teacherService.list();
        PageInfo<Teacher> pageInfo = new PageInfo<>(list);
        return Result.success(PageResult.of(pageInfo.getList(), pageInfo.getTotal(), pageNum, pageSize));
    }

    /**
     * 获取教师列表（管理员/教师可看）
     */
    @GetMapping("/teacher/list")
    public Result<List<Teacher>> listTeacher() {
        String role = (String) StpUtil.getSession().get("role");
        if ("admin".equals(role) || "teacher".equals(role)) {
            return Result.success(teacherService.list());
        }
        throw new BusinessException("无权限");
    }

    /**
     * 获取学生列表（分页）
     */
    @GetMapping("/student/page")
    public Result<PageResult<Student>> listStudentPage(
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize) {
        String role = (String) StpUtil.getSession().get("role");
        if (!"admin".equals(role) && !"teacher".equals(role)) {
            throw new BusinessException("无权限");
        }
        PageHelper.startPage(pageNum, pageSize);
        List<Student> list = studentService.list();
        PageInfo<Student> pageInfo = new PageInfo<>(list);
        return Result.success(PageResult.of(pageInfo.getList(), pageInfo.getTotal(), pageNum, pageSize));
    }

    /**
     * 获取学生列表（管理员/教师可看）
     */
    @GetMapping("/student/list")
    public Result<List<Student>> listStudent() {
        String role = (String) StpUtil.getSession().get("role");
        if ("admin".equals(role) || "teacher".equals(role)) {
            return Result.success(studentService.list());
        }
        throw new BusinessException("无权限");
    }

    /**
     * 新增管理员
     */
    @PostMapping("/admin")
    @SaCheckRole("admin")
    public Result<Void> addAdmin(@RequestBody Admin admin) {
        adminService.add(admin);
        return Result.success();
    }

    /**
     * 新增教师
     */
    @PostMapping("/teacher")
    @SaCheckRole("admin")
    public Result<Void> addTeacher(@RequestBody Teacher teacher) {
        teacherService.add(teacher);
        return Result.success();
    }

    /**
     * 新增学生
     */
    @PostMapping("/student")
    @SaCheckRole("admin")
    public Result<Void> addStudent(@RequestBody Student student) {
        studentService.add(student);
        return Result.success();
    }

    /**
     * 修改管理员
     */
    @PutMapping("/admin")
    @SaCheckRole("admin")
    public Result<Void> updateAdmin(@RequestBody Admin admin) {
        adminService.update(admin);
        return Result.success();
    }

    /**
     * 修改教师
     */
    @PutMapping("/teacher")
    @SaCheckRole("admin")
    public Result<Void> updateTeacher(@RequestBody Teacher teacher) {
        teacherService.update(teacher);
        return Result.success();
    }

    /**
     * 修改学生
     */
    @PutMapping("/student")
    @SaCheckRole("admin")
    public Result<Void> updateStudent(@RequestBody Student student) {
        studentService.update(student);
        return Result.success();
    }

    /**
     * 删除管理员
     */
    @DeleteMapping("/admin/{id}")
    @SaCheckRole("admin")
    public Result<Void> deleteAdmin(@PathVariable Long id) {
        adminService.delete(id);
        return Result.success();
    }

    /**
     * 删除教师
     */
    @DeleteMapping("/teacher/{id}")
    @SaCheckRole("admin")
    public Result<Void> deleteTeacher(@PathVariable Long id) {
        teacherService.delete(id);
        return Result.success();
    }

    /**
     * 删除学生
     */
    @DeleteMapping("/student/{id}")
    @SaCheckRole("admin")
    public Result<Void> deleteStudent(@PathVariable Long id) {
        studentService.delete(id);
        return Result.success();
    }

    // ==================== 个人中心（所有角色） ====================

    /**
     * 获取当前登录用户信息
     */
    @GetMapping("/profile")
    public Result<Map<String, Object>> getProfile() {
        return Result.success(authService.getCurrentUserInfo());
    }

    /**
     * 修改个人信息（所有角色可改自己的基本信息）
     */
    @PutMapping("/profile")
    public Result<Void> updateProfile(@RequestBody Map<String, String> params) {
        String role = (String) StpUtil.getSession().get("role");
        Long realId = getRealIdFromSession();
        
        if (realId == null) {
            return Result.fail("用户信息获取失败");
        }

        if ("admin".equals(role)) {
            Admin admin = new Admin();
            admin.setId(realId);
            admin.setRealName(params.get("realName"));
            admin.setEmail(params.get("email"));
            admin.setPhone(params.get("phone"));
            admin.setAvatar(params.get("avatar"));
            adminService.update(admin);
        } else if ("teacher".equals(role)) {
            Teacher teacher = new Teacher();
            teacher.setId(realId);
            teacher.setRealName(params.get("realName"));
            teacher.setEmail(params.get("email"));
            teacher.setPhone(params.get("phone"));
            teacher.setAvatar(params.get("avatar"));
            teacherService.update(teacher);
        } else if ("student".equals(role)) {
            Student student = new Student();
            student.setId(realId);
            student.setRealName(params.get("realName"));
            student.setEmail(params.get("email"));
            student.setPhone(params.get("phone"));
            student.setAvatar(params.get("avatar"));
            studentService.update(student);
        }
        return Result.success();
    }

    /**
     * 修改密码
     */
    @PutMapping("/password")
    public Result<Void> changePassword(@RequestBody Map<String, String> params) {
        String oldPassword = params.get("oldPassword");
        String newPassword = params.get("newPassword");
        String role = (String) StpUtil.getSession().get("role");
        Long realId = getRealIdFromSession();
        
        if (realId == null) {
            return Result.fail("用户信息获取失败");
        }

        String storedPwd = null;
        if ("admin".equals(role)) {
            Admin admin = adminService.getById(realId);
            // adminService.getById 已经脱密了密码，需要直接查
            Admin raw = adminService.getRawById(realId);
            storedPwd = raw != null ? raw.getPassword() : null;
        } else if ("teacher".equals(role)) {
            Teacher raw = teacherService.getRawById(realId);
            storedPwd = raw != null ? raw.getPassword() : null;
        } else if ("student".equals(role)) {
            Student raw = studentService.getRawById(realId);
            storedPwd = raw != null ? raw.getPassword() : null;
        }

        if (storedPwd == null || !storedPwd.equals(oldPassword)) {
            throw new BusinessException("原密码错误");
        }

        if ("admin".equals(role)) {
            Admin admin = new Admin();
            admin.setId(realId);
            admin.setPassword(newPassword);
            adminService.update(admin);
        } else if ("teacher".equals(role)) {
            Teacher teacher = new Teacher();
            teacher.setId(realId);
            teacher.setPassword(newPassword);
            teacherService.update(teacher);
        } else if ("student".equals(role)) {
            Student student = new Student();
            student.setId(realId);
            student.setPassword(newPassword);
            studentService.update(student);
        }
        return Result.success();
    }

    // ==================== 课程学生管理（教师/学生） ====================

    /**
     * 获取课程下的学生列表（教师/管理员）
     */
    @GetMapping("/course-students/{courseId}")
    public Result<List<CourseStudent>> getCourseStudents(@PathVariable Long courseId) {
        String role = (String) StpUtil.getSession().get("role");
        if ("admin".equals(role) || "teacher".equals(role)) {
            return Result.success(courseStudentService.getStudentsByCourseId(courseId));
        }
        throw new BusinessException("无权限");
    }

    /**
     * 添加学生到课程（教师/管理员）
     */
    @PostMapping("/course-student")
    public Result<Void> addStudentToCourse(@RequestBody Map<String, Long> params) {
        String role = (String) StpUtil.getSession().get("role");
        if ("admin".equals(role) || "teacher".equals(role)) {
            courseStudentService.addStudentToCourse(params.get("courseId"), params.get("studentId"));
            return Result.success();
        }
        throw new BusinessException("无权限");
    }

    /**
     * 从课程移除学生（教师/管理员）
     */
    @DeleteMapping("/course-student")
    public Result<Void> removeStudentFromCourse(@RequestParam Long courseId, @RequestParam Long studentId) {
        String role = (String) StpUtil.getSession().get("role");
        if ("admin".equals(role) || "teacher".equals(role)) {
            courseStudentService.removeStudentFromCourse(courseId, studentId);
            return Result.success();
        }
        throw new BusinessException("无权限");
    }

    /**
     * 获取当前学生的选课列表（学生）
     */
    @GetMapping("/my-courses")
    public Result<List<CourseStudent>> getMyCourses() {
        Long realId = getRealIdFromSession();
        if (realId == null) {
            return Result.fail("用户信息获取失败");
        }
        return Result.success(courseStudentService.getCoursesByStudentId(realId));
    }

    /**
     * 获取当前学生的选课列表（分页）
     */
    @GetMapping("/my-courses/page")
    public Result<PageResult<CourseStudent>> getMyCoursesPage(
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize) {
        Long realId = getRealIdFromSession();
        if (realId == null) {
            return Result.fail("用户信息获取失败");
        }
        PageHelper.startPage(pageNum, pageSize);
        List<CourseStudent> list = courseStudentService.getCoursesByStudentId(realId);
        PageInfo<CourseStudent> pageInfo = new PageInfo<>(list);
        return Result.success(PageResult.of(pageInfo.getList(), pageInfo.getTotal(), pageNum, pageSize));
    }
}