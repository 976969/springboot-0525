package com.qcby.springboot_0525.entity;

import lombok.Data;
import java.util.Date;

@Data
public class Course {
    private Long id;
    private String name;
    private String description;
    private Long teacherId;
    private Integer status;
    private Date createTime;
    private Date updateTime;

    // 关联查询字段
    private String teacherName;
}
