package com.qcby.springboot_0525.entity;

import lombok.Data;
import java.util.Date;

@Data
public class TrainingTask {
    private Long id;
    private Long courseId;
    private String title;
    private String description;
    private String requirements;
    private Date deadline;
    private Integer status;
    private Date createTime;
    private Date updateTime;

    // 关联查询字段
    private String courseName;
}
