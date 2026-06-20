package com.qcby.springboot_0525.entity;

import lombok.Data;
import java.math.BigDecimal;
import java.util.Date;

/**
 * 评价记录实体类
 */
@Data
public class EvaluationRecord {
    /** 主键ID */
    private Long id;
    /** 成果ID */
    private Long resultId;
    /** 指标ID */
    private Long indicatorId;
    /** AI评分 */
    private BigDecimal aiScore;
    /** 教师评分 */
    private BigDecimal teacherScore;
    /** 最终得分 */
    private BigDecimal finalScore;
    /** 评分比重（AI权重） */
    private BigDecimal scoreRatio;
    /** AI评语 */
    private String aiComment;
    /** 教师评语 */
    private String teacherComment;
    /** 创建时间 */
    private Date createTime;
    /** 更新时间 */
    private Date updateTime;

    /** 关联查询字段：指标名称 */
    private String indicatorName;
    /** 关联查询字段：指标权重 */
    private BigDecimal indicatorWeight;
}
