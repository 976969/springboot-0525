package com.qcby.springboot_0525.entity;

import lombok.Data;
import java.util.Date;

/**
 * 验证码实体（邮箱/手机验证码）
 */
@Data
public class VerificationCode {
    /** 主键ID */
    private Long id;
    /** 目标地址（邮箱或手机号） */
    private String target;
    /** 验证码（6位数字） */
    private String code;
    /** 类型：email / phone */
    private String type;
    /** 是否已使用：0-未使用，1-已使用 */
    private Integer used;
    /** 过期时间 */
    private Date expireTime;
    /** 创建时间 */
    private Date createTime;
}
