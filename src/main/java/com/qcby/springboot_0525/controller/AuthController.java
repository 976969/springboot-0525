package com.qcby.springboot_0525.controller;

import com.qcby.springboot_0525.common.Result;
import com.qcby.springboot_0525.service.AuthService;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.Map;

/**
 * 认证控制器（登录、登出、用户信息）
 */
@RestController
@RequestMapping("/auth")
public class AuthController {

    @Resource
    private AuthService authService;

    /**
     * 登录（前端指定身份）
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
     * 获取当前用户信息（根据角色查不同表）
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