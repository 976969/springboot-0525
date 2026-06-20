package com.qcby.springboot_0525.entity;

import lombok.Data;
import java.util.Date;

/**
 * 核查记录实体类
 */
@Data
public class CheckRecord {
    /** 主键ID */
    private Long id;
    /** 成果ID */
    private Long resultId;
    /** 核查类型 */
    private String checkType;
    /** 核查结果：0-不通过，1-通过 */
    private Integer checkResult;
    /** 问题描述 */
    private String issueDescription;
    /** 改进建议 */
    private String suggestion;
    /** 创建时间 */
    private Date createTime;
}
