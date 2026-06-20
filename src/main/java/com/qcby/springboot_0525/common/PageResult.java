package com.qcby.springboot_0525.common;

import lombok.Data;
import java.util.List;

/**
 * 分页返回结果
 */
@Data
public class PageResult<T> {
    /** 数据列表 */
    private List<T> list;
    /** 总记录数 */
    private long total;
    /** 当前页码 */
    private int pageNum;
    /** 每页大小 */
    private int pageSize;
    /** 总页数 */
    private int totalPages;

    /**
     * 分页结果构造函数
     */
    public PageResult(List<T> list, long total, int pageNum, int pageSize) {
        this.list = list;
        this.total = total;
        this.pageNum = pageNum;
        this.pageSize = pageSize;
        this.totalPages = pageSize > 0 ? (int) Math.ceil((double) total / pageSize) : 0;
    }

    /**
     * 创建分页结果
     */
    public static <T> PageResult<T> of(List<T> list, long total, int pageNum, int pageSize) {
        return new PageResult<>(list, total, pageNum, pageSize);
    }
}
