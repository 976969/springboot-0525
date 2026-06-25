package com.qcby.springboot_0525.controller;

import com.qcby.springboot_0525.common.Result;
import com.qcby.springboot_0525.service.AuthService;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.Map;

/**
 * 认证控制器（登录、注册、登出、用户信息、验证码）
 */
@RestController
@RequestMapping("/auth")
public class AuthController {

    @Resource
    private AuthService authService;

    /**
     * 用户名+密码登录（原有接口，保持不变）
     */
    @PostMapping("/login")
    public Result<Map<String, Object>> login(@RequestBody Map<String, String> params) {
        String username = params.get("username");
        String password = params.get("password");
        String role = params.get("role");
        Map<String, Object> data = authService.login(username, password, role);
        return Result.success(data);
    }

    /**
     * 手机号+验证码登录
     */
    @PostMapping("/login-by-phone")
    public Result<Map<String, Object>> loginByPhone(@RequestBody Map<String, String> params) {
        String phone = params.get("phone");
        String code = params.get("code");
        String role = params.get("role");
        Map<String, Object> data = authService.loginByPhone(phone, code, role);
        return Result.success(data);
    }

    /**
     * 邮箱验证码登录
     */
    @PostMapping("/login-by-email")
    public Result<Map<String, Object>> loginByEmail(@RequestBody Map<String, String> params) {
        String email = params.get("email");
        String code = params.get("code");
        String role = params.get("role");
        Map<String, Object> data = authService.loginByEmailCode(email, code, role);
        return Result.success(data);
    }

    /**
     * 学生注册
     */
    @PostMapping("/register")
    public Result<Map<String, Object>> register(@RequestBody Map<String, String> params) {
        String username = params.get("username");
        String password = params.get("password");
        String realName = params.get("realName");
        String email = params.get("email");
        String phone = params.get("phone");
        String studentNo = params.get("studentNo");
        String className = params.get("className");
        String code = params.get("code");
        Map<String, Object> data = authService.register(username, password, realName,
                email, phone, studentNo, className, code);
        return Result.success(data);
    }

    /**
     * 发送邮箱验证码
     */
    @PostMapping("/send-code")
    public Result<Void> sendCode(@RequestBody Map<String, String> params) {
        String email = params.get("email");
        authService.sendEmailCode(email);
        return Result.success();
    }

    /**
     * 发送手机验证码
     */
    @PostMapping("/send-phone-code")
    public Result<Void> sendPhoneCode(@RequestBody Map<String, String> params) {
        String phone = params.get("phone");
        authService.sendPhoneCode(phone);
        return Result.success();
    }

    /**
     * 获取当前用户信息
     */
    @GetMapping("/info")
    public Result<Map<String, Object>> info() {
        return Result.success(authService.getCurrentUserInfo());
    }

    /**
     * 退出登录
     */
    @PostMapping("/logout")
    public Result<Void> logout() {
        authService.logout();
        return Result.success();
    }
}
