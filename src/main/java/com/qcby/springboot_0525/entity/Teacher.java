package com.qcby.springboot_0525.entity;

import lombok.Data;
import java.util.Date;

@Data
public class Teacher {
    private Long id;
    private String username;
    private String password;
    private String realName;
    private String avatar;
    private String email;
    private String phone;
    private String department;
    private String title;
    private Integer status;
    private Date createTime;
    private Date updateTime;
}