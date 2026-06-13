package com.qcby.springboot_0525.config;

import cn.dev33.satoken.stp.StpInterface;
import cn.dev33.satoken.stp.StpUtil;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * Sa-Token 权限/角色获取接口实现
 * 让 @SaCheckRole 注解能够正确工作
 */
@Component
public class SaTokenConfig implements StpInterface {

    @Override
    public List<String> getPermissionList(Object loginId, String loginType) {
        // 本项目不使用细粒度权限，返回空
        return new ArrayList<>();
    }

    @Override
    public List<String> getRoleList(Object loginId, String loginType) {
        // 从session中获取角色
        String role = (String) StpUtil.getSessionByLoginId(loginId).get("role");
        List<String> roles = new ArrayList<>();
        if (role != null) {
            roles.add(role);
        }
        return roles;
    }
}
