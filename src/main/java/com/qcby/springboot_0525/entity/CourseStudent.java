package com.qcby.springboot_0525.entity;

import lombok.Data;
import java.util.Date;

@Data
public class CourseStudent {
    private Long id;
    private Long courseId;
    private Long studentId;
    private Date createTime;

    // 关联查询字段
    private String courseName;
    private String studentName;
    private String studentUsername;
    private String teacherName;
}
