package com.qcby.springboot_0525.entity;

import lombok.Data;
import java.util.Date;

/**
 * 课程学生关联实体类
 */
@Data
public class CourseStudent {
    /** 主键ID */
    private Long id;
    /** 课程ID */
    private Long courseId;
    /** 学生ID */
    private Long studentId;
    /** 创建时间 */
    private Date createTime;

    /** 关联查询字段：课程名称 */
    private String courseName;
    /** 关联查询字段：学生姓名 */
    private String studentName;
    /** 关联查询字段：学生用户名 */
    private String studentUsername;
    /** 关联查询字段：学生班级 */
    private String className;
    /** 关联查询字段：教师姓名 */
    private String teacherName;
}
