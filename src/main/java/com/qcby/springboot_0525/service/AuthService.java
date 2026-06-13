package com.qcby.springboot_0525.service;

import cn.dev33.satoken.stp.StpUtil;
import com.qcby.springboot_0525.common.BusinessException;
import com.qcby.springboot_0525.entity.Admin;
import com.qcby.springboot_0525.entity.Student;
import com.qcby.springboot_0525.entity.Teacher;
import com.qcby.springboot_0525.mapper.AdminMapper;
import com.qcby.springboot_0525.mapper.StudentMapper;
import com.qcby.springboot_0525.mapper.TeacherMapper;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.Map;

/**
 * 认证服务 - 登录时依次查 admin、teacher、student 三张表
 */
@Service
public class AuthService {

    @Resource
    private AdminMapper adminMapper;

    @Resource
    private TeacherMapper teacherMapper;

    @Resource
    private StudentMapper studentMapper;

    /**
     * 指定角色登录：只查对应表
     */
    public Map<String, Object> login(String username, String password, String role) {
        if ("admin".equals(role)) {
            Admin admin = adminMapper.selectByUsername(username);
            if (admin == null) {
                throw new BusinessException("管理员账号不存在");
            }
            return doLogin(admin.getId(), admin.getPassword(), password, admin, "admin");
        } else if ("teacher".equals(role)) {
            Teacher teacher = teacherMapper.selectByUsername(username);
            if (teacher == null) {
                throw new BusinessException("教师账号不存在");
            }
            return doLogin(teacher.getId(), teacher.getPassword(), password, teacher, "teacher");
        } else if ("student".equals(role)) {
            Student student = studentMapper.selectByUsername(username);
            if (student == null) {
                throw new BusinessException("学生账号不存在");
            }
            return doLogin(student.getId(), student.getPassword(), password, student, "student");
        }
        throw new BusinessException("无效的角色类型");
    }

    private Map<String, Object> doLogin(Long id, String storedPwd, String inputPwd, Object user, String role) {
        if (!storedPwd.equals(inputPwd)) {
            throw new BusinessException("密码错误");
        }
        // 检查状态
        Integer status = null;
        if (user instanceof Admin) status = ((Admin) user).getStatus();
        else if (user instanceof Teacher) status = ((Teacher) user).getStatus();
        else if (user instanceof Student) status = ((Student) user).getStatus();

        if (status == null || status != 1) {
            throw new BusinessException("账号已被禁用");
        }

        // Sa-Token 登录，使用 "角色:ID" 格式避免不同表ID冲突
        String loginId = role + ":" + id;
        StpUtil.login(loginId);
        // 将角色和真实ID存入session
        StpUtil.getSession().set("role", role);
        StpUtil.getSession().set("realId", id);

        Map<String, Object> result = new HashMap<>();
        result.put("token", StpUtil.getTokenValue());

        Map<String, Object> userInfo = new HashMap<>();
        userInfo.put("id", id);
        userInfo.put("role", role);
        if (user instanceof Admin) {
            Admin a = (Admin) user;
            userInfo.put("username", a.getUsername());
            userInfo.put("realName", a.getRealName());
            userInfo.put("avatar", a.getAvatar());
            userInfo.put("email", a.getEmail());
            userInfo.put("phone", a.getPhone());
        } else if (user instanceof Teacher) {
            Teacher t = (Teacher) user;
            userInfo.put("username", t.getUsername());
            userInfo.put("realName", t.getRealName());
            userInfo.put("avatar", t.getAvatar());
            userInfo.put("email", t.getEmail());
            userInfo.put("phone", t.getPhone());
            userInfo.put("department", t.getDepartment());
            userInfo.put("title", t.getTitle());
        } else if (user instanceof Student) {
            Student s = (Student) user;
            userInfo.put("username", s.getUsername());
            userInfo.put("realName", s.getRealName());
            userInfo.put("avatar", s.getAvatar());
            userInfo.put("email", s.getEmail());
            userInfo.put("phone", s.getPhone());
            userInfo.put("studentNo", s.getStudentNo());
            userInfo.put("className", s.getClassName());
        }
        result.put("user", userInfo);
        return result;
    }

    /**
     * 获取当前登录用户信息（根据角色查不同表）
     */
    public Map<String, Object> getCurrentUserInfo() {
        String role = (String) StpUtil.getSession().get("role");
        Object realIdObj = StpUtil.getSession().get("realId");
        
        // 类型转换处理
        Long realId = null;
        if (realIdObj instanceof Long) {
            realId = (Long) realIdObj;
        } else if (realIdObj instanceof Integer) {
            realId = ((Integer) realIdObj).longValue();
        } else if (realIdObj instanceof String) {
            realId = Long.parseLong((String) realIdObj);
        }
        
        if (realId == null) {
            throw new BusinessException("用户信息获取失败");
        }

        Map<String, Object> userInfo = new HashMap<>();
        userInfo.put("id", realId);
        userInfo.put("role", role);

        if ("admin".equals(role)) {
            Admin admin = adminMapper.selectById(realId);
            if (admin == null) throw new BusinessException("用户不存在");
            userInfo.put("username", admin.getUsername());
            userInfo.put("realName", admin.getRealName());
            userInfo.put("avatar", admin.getAvatar());
            userInfo.put("email", admin.getEmail());
            userInfo.put("phone", admin.getPhone());
        } else if ("teacher".equals(role)) {
            Teacher teacher = teacherMapper.selectById(realId);
            if (teacher == null) throw new BusinessException("用户不存在");
            userInfo.put("username", teacher.getUsername());
            userInfo.put("realName", teacher.getRealName());
            userInfo.put("avatar", teacher.getAvatar());
            userInfo.put("email", teacher.getEmail());
            userInfo.put("phone", teacher.getPhone());
            userInfo.put("department", teacher.getDepartment());
            userInfo.put("title", teacher.getTitle());
        } else if ("student".equals(role)) {
            Student student = studentMapper.selectById(realId);
            if (student == null) throw new BusinessException("用户不存在");
            userInfo.put("username", student.getUsername());
            userInfo.put("realName", student.getRealName());
            userInfo.put("avatar", student.getAvatar());
            userInfo.put("email", student.getEmail());
            userInfo.put("phone", student.getPhone());
            userInfo.put("studentNo", student.getStudentNo());
            userInfo.put("className", student.getClassName());
        }
        return userInfo;
    }

    /**
     * 退出登录
     */
    public void logout() {
        StpUtil.logout();
    }
}