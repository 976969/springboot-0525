package com.qcby.springboot_0525.entity;

import lombok.Data;
import java.util.Date;

@Data
public class Banner {
    private Long id;
    private String title;
    private String content;
    private String imageUrl;
    private String linkUrl;
    private Integer sort;
    private Integer status; // 0=下架 1=上架
    private Date createTime;
    private Date updateTime;
}
