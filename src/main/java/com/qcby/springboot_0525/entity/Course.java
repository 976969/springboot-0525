package com.qcby.springboot_0525.entity;

import lombok.Data;
import java.util.Date;

/**
 * 课程实体类
 */
@Data
public class Course {
    /** 主键ID */
    private Long id;
    /** 课程名称 */
    private String name;
    /** 课程描述 */
    private String description;
    /** 教师ID */
    private Long teacherId;
    /** 状态：0-停用，1-启用 */
    private Integer status;
    /** 创建时间 */
    private Date createTime;
    /** 更新时间 */
    private Date updateTime;

    /** 关联查询字段：教师姓名 */
    private String teacherName;
    /** 关联查询字段：选课人数 */
    private Integer studentCount;
}
