package com.qcby.springboot_0525.entity;

import lombok.Data;
import java.math.BigDecimal;
import java.util.Date;

@Data
public class EvaluationRecord {
    private Long id;
    private Long resultId;
    private Long indicatorId;
    private BigDecimal aiScore;
    private BigDecimal teacherScore;
    private BigDecimal finalScore;
    private String aiComment;
    private String teacherComment;
    private Date createTime;
    private Date updateTime;

    // 关联查询字段
    private String indicatorName;
    private BigDecimal indicatorWeight;
}
