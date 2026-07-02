package com.qcby.springboot_0525.entity;

import java.util.Date;

/**
 * 验证码实体类
 */
public class VerificationCode {

    private Long id;
    private String target;
    private String code;
    private String type;
    private Integer used;
    private Date expireTime;
    private Date createTime;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getTarget() { return target; }
    public void setTarget(String target) { this.target = target; }

    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public Integer getUsed() { return used; }
    public void setUsed(Integer used) { this.used = used; }

    public Date getExpireTime() { return expireTime; }
    public void setExpireTime(Date expireTime) { this.expireTime = expireTime; }

    public Date getCreateTime() { return createTime; }
    public void setCreateTime(Date createTime) { this.createTime = createTime; }
}
