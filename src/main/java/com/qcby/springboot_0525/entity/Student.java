package com.qcby.springboot_0525.entity;

import lombok.Data;
import java.util.Date;

/**
 * 学生实体类
 */
@Data
public class Student {
    /** 主键ID */
    private Long id;
    /** 用户名 */
    private String username;
    /** 密码 */
    private String password;
    /** 真实姓名 */
    private String realName;
    /** 头像 */
    private String avatar;
    /** 邮箱 */
    private String email;
    /** 手机号 */
    private String phone;
    /** 学号 */
    private String studentNo;
    /** 班级 */
    private String className;
    /** 状态：0-禁用，1-启用 */
    private Integer status;
    /** 创建时间 */
    private Date createTime;
    /** 更新时间 */
    private Date updateTime;
}