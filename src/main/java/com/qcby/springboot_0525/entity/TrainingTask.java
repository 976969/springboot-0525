package com.qcby.springboot_0525.entity;

import lombok.Data;
import java.util.Date;

/**
 * 实训任务实体类
 */
@Data
public class TrainingTask {
    /** 主键ID */
    private Long id;
    /** 课程ID */
    private Long courseId;
    /** 任务标题 */
    private String title;
    /** 任务描述 */
    private String description;
    /** 任务要求 */
    private String requirements;
    /** 截止时间 */
    private Date deadline;
    /** 状态：1-进行中，3-已过期 */
    private Integer status;
    /** 创建时间 */
    private Date createTime;
    /** 更新时间 */
    private Date updateTime;

    /** 关联查询字段：课程名称 */
    private String courseName;

    /** 关联查询字段：教师ID */
    private Long teacherId;

    /** 关联查询字段：教师名称 */
    private String teacherName;
}
