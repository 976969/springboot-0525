package com.qcby.springboot_0525.entity;

import lombok.Data;
import java.math.BigDecimal;
import java.util.Date;

/**
 * 评价报告实体类
 */
@Data
public class EvaluationReport {
    /** 主键ID */
    private Long id;
    /** 任务ID */
    private Long taskId;
    /** 学生ID */
    private Long studentId;
    /** 总分（AI加权得分） */
    private BigDecimal totalScore;
    /** 教师评分（整体评分） */
    private BigDecimal teacherScore;
    /** 教师评分占比（0-10，表示AI评分占比） */
    private BigDecimal teacherScoreRatio;
    /** 评分教师ID */
    private Long teacherId;
    /** 报告数据（JSON格式） */
    private String reportData;
    /** 导出格式 */
    private String exportFormat;
    /** 创建时间 */
    private Date createTime;

    /** 关联查询字段：学生姓名 */
    private String studentName;
    /** 关联查询字段：任务标题 */
    private String taskTitle;
    /** 关联查询字段：文件名 */
    private String fileName;
    /** 关联查询字段：教师姓名 */
    private String teacherName;
}
