package com.qcby.springboot_0525.entity;

import lombok.Data;
import java.util.Date;

/**
 * 课程安排实体类
 */
@Data
public class ClassSchedule {
    /** 主键ID */
    private Long id;
    /** 课程ID */
    private Long courseId;
    /** 教师ID */
    private Long teacherId;
    /** 星期几：1-7 */
    private Integer dayOfWeek;
    /** 开始时间 */
    private String startTime;
    /** 结束时间 */
    private String endTime;
    /** 上课地点 */
    private String location;
    /** 备注 */
    private String remark;
    /** 创建时间 */
    private Date createTime;
    /** 更新时间 */
    private Date updateTime;

    /** 关联查询字段：课程名称 */
    private String courseName;
    /** 关联查询字段：教师姓名 */
    private String teacherName;
}
