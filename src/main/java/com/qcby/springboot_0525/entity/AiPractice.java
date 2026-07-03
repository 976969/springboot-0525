package com.qcby.springboot_0525.entity;

import lombok.Data;
import java.util.Date;

/**
 * AI练习题实体类
 */
@Data
public class AiPractice {
    /** 主键ID */
    private Long id;
    /** 学生ID */
    private Long studentId;
    /** 课程ID */
    private Long courseId;
    /** 课程名称 */
    private String courseName;
    /** 题目数据(JSON数组) */
    private String questions;
    /** 学生答案(JSON) */
    private String answers;
    /** 得分 */
    private Double score;
    /** 总分 */
    private Double totalScore;
    /** 创建时间 */
    private Date createTime;

    /** 关联查询字段：学生姓名 */
    private String studentName;
}
