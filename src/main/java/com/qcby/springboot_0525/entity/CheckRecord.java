package com.qcby.springboot_0525.entity;

import lombok.Data;
import java.util.Date;

@Data
public class CheckRecord {
    private Long id;
    private Long resultId;
    private String checkType;
    private Integer checkResult;
    private String issueDescription;
    private String suggestion;
    private Date createTime;
}
