package com.qcby.springboot_0525.entity;

import lombok.Data;
import java.math.BigDecimal;
import java.util.Date;

@Data
public class EvaluationReport {
    private Long id;
    private Long taskId;
    private Long studentId;
    private BigDecimal totalScore;
    private String reportData;
    private String exportFormat;
    private Date createTime;

    // 关联查询字段
    private String studentName;
    private String taskTitle;
}
