package com.qcby.springboot_0525.enums;

import lombok.Getter;

/**
 * 用户角色枚举
 */
@Getter
public enum RoleEnum {

    ADMIN("admin", "管理员"),
    TEACHER("teacher", "教师"),
    STUDENT("student", "学生");

    private final String code;
    private final String name;

    RoleEnum(String code, String name) {
        this.code = code;
        this.name = name;
    }
}
