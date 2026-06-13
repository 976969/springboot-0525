package com.qcby.springboot_0525.entity;

import lombok.Data;
import java.math.BigDecimal;
import java.util.Date;

@Data
public class EvaluationIndicator {
    private Long id;
    private String name;
    private String description;
    private String category;
    private BigDecimal defaultWeight;
    private Integer isSystem;
    private Date createTime;
    private Date updateTime;
}
