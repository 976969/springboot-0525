package com.qcby.springboot_0525.entity;

import lombok.Data;
import java.util.Date;

@Data
public class Admin {
    private Long id;
    private String username;
    private String password;
    private String realName;
    private String avatar;
    private String email;
    private String phone;
    private Integer status;
    private Date createTime;
    private Date updateTime;
}