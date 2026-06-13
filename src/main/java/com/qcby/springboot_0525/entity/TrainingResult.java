package com.qcby.springboot_0525.entity;

import lombok.Data;
import java.util.Date;

@Data
public class TrainingResult {
    private Long id;
    private Long taskId;
    private Long studentId;
    private String filePath;
    private String fileName;
    private String fileType;
    private Long fileSize;
    private String parsedContent;
    private Date uploadTime;
    private Integer status;
    private Date createTime;

    // 关联查询字段
    private String studentName;
    private String taskTitle;
}
