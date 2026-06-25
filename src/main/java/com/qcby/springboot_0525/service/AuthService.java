package com.qcby.springboot_0525.service;

import cn.dev33.satoken.stp.StpUtil;
import com.qcby.springboot_0525.common.BusinessException;
import com.qcby.springboot_0525.entity.Admin;
import com.qcby.springboot_0525.entity.Student;
import com.qcby.springboot_0525.entity.Teacher;
import com.qcby.springboot_0525.entity.VerificationCode;
import com.qcby.springboot_0525.mapper.AdminMapper;
import com.qcby.springboot_0525.mapper.StudentMapper;
import com.qcby.springboot_0525.mapper.TeacherMapper;
import com.qcby.springboot_0525.mapper.VerificationCodeMapper;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.Resource;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

/**
 * 认证服务 - 登录、注册、验证码
 */
@Service
public class AuthService {

    @Resource
    private AdminMapper adminMapper;

    @Resource
    private TeacherMapper teacherMapper;

    @Resource
    private StudentMapper studentMapper;

    @Resource
    private VerificationCodeMapper verificationCodeMapper;

    @Autowired(required = false)
    private JavaMailSender mailSender;

    // ==================== 用户名+密码登录（原有逻辑） ====================

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

    // ==================== 手机号+验证码登录 ====================

    /**
     * 手机号+验证码登录
     */
    public Map<String, Object> loginByPhone(String phone, String code, String role) {
        // 1. 校验验证码
        validateCode(phone, code, "phone");

        // 2. 根据角色查对应表
        if ("admin".equals(role)) {
            Admin admin = adminMapper.selectByPhone(phone);
            if (admin == null) {
                throw new BusinessException("该手机号未注册管理员账号");
            }
            if (admin.getStatus() != 1) {
                throw new BusinessException("账号已被禁用");
            }
            return buildLoginResult(admin.getId(), admin, "admin");
        } else if ("teacher".equals(role)) {
            Teacher teacher = teacherMapper.selectByPhone(phone);
            if (teacher == null) {
                throw new BusinessException("该手机号未注册教师账号");
            }
            if (teacher.getStatus() != 1) {
                throw new BusinessException("账号已被禁用");
            }
            return buildLoginResult(teacher.getId(), teacher, "teacher");
        } else if ("student".equals(role)) {
            Student student = studentMapper.selectByPhone(phone);
            if (student == null) {
                throw new BusinessException("该手机号未注册学生账号");
            }
            if (student.getStatus() != 1) {
                throw new BusinessException("账号已被禁用");
            }
            return buildLoginResult(student.getId(), student, "student");
        }
        throw new BusinessException("无效的角色类型");
    }

    // ==================== 邮箱验证码登录 ====================

    /**
     * 邮箱验证码登录
     */
    public Map<String, Object> loginByEmailCode(String email, String code, String role) {
        // 1. 校验验证码
        validateCode(email, code, "email");

        // 2. 根据角色查对应表
        if ("admin".equals(role)) {
            Admin admin = adminMapper.selectByEmail(email);
            if (admin == null) {
                throw new BusinessException("该邮箱未注册管理员账号");
            }
            if (admin.getStatus() != 1) {
                throw new BusinessException("账号已被禁用");
            }
            return buildLoginResult(admin.getId(), admin, "admin");
        } else if ("teacher".equals(role)) {
            Teacher teacher = teacherMapper.selectByEmail(email);
            if (teacher == null) {
                throw new BusinessException("该邮箱未注册教师账号");
            }
            if (teacher.getStatus() != 1) {
                throw new BusinessException("账号已被禁用");
            }
            return buildLoginResult(teacher.getId(), teacher, "teacher");
        } else if ("student".equals(role)) {
            Student student = studentMapper.selectByEmail(email);
            if (student == null) {
                throw new BusinessException("该邮箱未注册学生账号");
            }
            if (student.getStatus() != 1) {
                throw new BusinessException("账号已被禁用");
            }
            return buildLoginResult(student.getId(), student, "student");
        }
        throw new BusinessException("无效的角色类型");
    }

    // ==================== 学生注册 ====================

    /**
     * 学生注册（邮箱验证码）
     */
    public Map<String, Object> register(String username, String password, String realName,
                                        String email, String phone, String studentNo,
                                        String className, String code) {
        // 1. 校验验证码
        validateCode(email, code, "email");

        // 2. 校验用户名唯一性
        Student existUser = studentMapper.selectByUsername(username);
        if (existUser != null) {
            throw new BusinessException("用户名已存在");
        }

        // 3. 校验邮箱唯一性
        Student existEmail = studentMapper.selectByEmail(email);
        if (existEmail != null) {
            throw new BusinessException("该邮箱已被注册");
        }

        // 4. 密码长度校验
        if (password == null || password.length() < 6) {
            throw new BusinessException("密码长度不能少于6位");
        }

        // 5. 创建学生记录
        Student student = new Student();
        student.setUsername(username);
        student.setPassword(password);
        student.setRealName(realName);
        student.setEmail(email);
        student.setPhone(phone);
        student.setStudentNo(studentNo);
        student.setClassName(className);
        student.setStatus(1);
        studentMapper.insert(student);

        // 6. 自动登录
        return buildLoginResult(student.getId(), student, "student");
    }

    // ==================== 发送验证码 ====================

    /**
     * 发送邮箱验证码
     */
    public void sendEmailCode(String email) {
        // 1. 校验邮箱格式
        if (email == null || !email.contains("@")) {
            throw new BusinessException("邮箱格式不正确");
        }

        // 2. 频率限制：60秒内只能发送一次
        VerificationCode latest = verificationCodeMapper.selectLatestByTargetAndType(email, "email");
        if (latest != null) {
            long diff = System.currentTimeMillis() - latest.getCreateTime().getTime();
            if (diff < 60 * 1000) {
                throw new BusinessException("验证码发送过于频繁，请60秒后再试");
            }
        }

        // 3. 生成6位随机验证码
        String code = String.format("%06d", new Random().nextInt(1000000));

        // 4. 保存到数据库（5分钟有效）
        VerificationCode vc = new VerificationCode();
        vc.setTarget(email);
        vc.setCode(code);
        vc.setType("email");
        vc.setUsed(0);
        vc.setExpireTime(new Date(System.currentTimeMillis() + 5 * 60 * 1000));
        verificationCodeMapper.insert(vc);

        // 5. 尝试发送邮件（网络不通时自动降级为控制台输出）
        boolean sent = false;
        if (mailSender != null) {
            try {
                SimpleMailMessage message = new SimpleMailMessage();
                message.setTo(email);
                message.setSubject("智能实训评价系统 - 邮箱验证码");
                message.setText("您的验证码是：" + code + "，有效期5分钟，请勿泄露给他人。");
                mailSender.send(message);
                sent = true;
            } catch (Exception e) {
                // 邮件发送失败（通常网络不通），降级为控制台输出，不影响功能
                System.out.println("[邮件发送失败] " + e.getMessage());
            }
        }
        if (!sent) {
            System.out.println("========== 验证码 ==========");
            System.out.println("收件邮箱: " + email);
            System.out.println("验证码: " + code);
            System.out.println("============================");
        }
    }

    /**
     * 发送手机验证码（暂用控制台输出，后续可接入短信平台）
     */
    public void sendPhoneCode(String phone) {
        // 1. 校验手机号格式
        if (phone == null || !phone.matches("^1[3-9]\\d{9}$")) {
            throw new BusinessException("手机号格式不正确");
        }

        // 2. 频率限制：60秒内只能发送一次
        VerificationCode latest = verificationCodeMapper.selectLatestByTargetAndType(phone, "phone");
        if (latest != null) {
            long diff = System.currentTimeMillis() - latest.getCreateTime().getTime();
            if (diff < 60 * 1000) {
                throw new BusinessException("验证码发送过于频繁，请60秒后再试");
            }
        }

        // 3. 生成6位随机验证码
        String code = String.format("%06d", new Random().nextInt(1000000));

        // 4. 保存到数据库（5分钟有效）
        VerificationCode vc = new VerificationCode();
        vc.setTarget(phone);
        vc.setCode(code);
        vc.setType("phone");
        vc.setUsed(0);
        vc.setExpireTime(new Date(System.currentTimeMillis() + 5 * 60 * 1000));
        verificationCodeMapper.insert(vc);

        // 5. 打印到控制台（后续接入短信平台后替换为真实发送）
        System.out.println("========== 手机验证码（请接入短信平台） ==========");
        System.out.println("手机号: " + phone);
        System.out.println("验证码: " + code);
        System.out.println("==================================================");
    }

    // ==================== 内部工具方法 ====================

    /**
     * 校验验证码
     */
    private void validateCode(String target, String code, String type) {
        VerificationCode vc = verificationCodeMapper.selectValidCode(target, code, type);
        if (vc == null) {
            throw new BusinessException("验证码错误或已过期");
        }
        // 标记为已使用
        verificationCodeMapper.updateUsed(vc.getId());
    }

    /**
     * 通用登录逻辑（密码校验 + Sa-Token登录）
     */
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

        return buildLoginResult(id, user, role);
    }

    /**
     * 构建登录返回结果（Sa-Token 登录 + 用户信息）
     */
    private Map<String, Object> buildLoginResult(Long id, Object user, String role) {
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
