package com.qcby.springboot_0525.entity;

import lombok.Data;
import java.util.Date;

/**
 * 实训成果实体类
 */
@Data
public class TrainingResult {
    /** 主键ID */
    private Long id;
    /** 任务ID */
    private Long taskId;
    /** 学生ID */
    private Long studentId;
    /** 文件路径 */
    private String filePath;
    /** 文件名称 */
    private String fileName;
    /** 文件类型 */
    private String fileType;
    /** 文件大小 */
    private Long fileSize;
    /** 解析内容 */
    private String parsedContent;
    /** 上传时间 */
    private Date uploadTime;
    /** 状态：0-待核查，1-已核查，2-已评价 */
    private Integer status;
    /** 创建时间 */
    private Date createTime;

    /** 关联查询字段：学生姓名 */
    private String studentName;
    /** 关联查询字段：任务标题 */
    private String taskTitle;
    /** 关联查询字段：课程ID */
    private Long courseId;
    /** 关联查询字段：课程名称 */
    private String courseName;
}
