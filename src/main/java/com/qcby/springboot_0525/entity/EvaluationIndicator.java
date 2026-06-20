package com.qcby.springboot_0525.entity;

import lombok.Data;
import java.math.BigDecimal;
import java.util.Date;

/**
 * 评价指标实体类
 */
@Data
public class EvaluationIndicator {
    /** 主键ID */
    private Long id;
    /** 指标名称 */
    private String name;
    /** 归属教师ID，0表示系统模板 */
    private Long teacherId;
    /** 指标描述 */
    private String description;
    /** 指标分类 */
    private String category;
    /** 默认权重 */
    private BigDecimal defaultWeight;
    /** 原始权重（用于重置） */
    private BigDecimal originalWeight;
    /** 是否系统指标：1-系统，0-自定义 */
    private Integer isSystem;
    /** 创建时间 */
    private Date createTime;
    /** 更新时间 */
    private Date updateTime;
}
