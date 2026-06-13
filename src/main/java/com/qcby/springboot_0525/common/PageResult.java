package com.qcby.springboot_0525.common;

import lombok.Data;
import java.util.List;

/**
 * 分页返回结果
 */
@Data
public class PageResult<T> {
    private List<T> list;      // 数据列表
    private long total;        // 总记录数
    private int pageNum;       // 当前页码
    private int pageSize;      // 每页大小
    private int totalPages;    // 总页数

    public PageResult(List<T> list, long total, int pageNum, int pageSize) {
        this.list = list;
        this.total = total;
        this.pageNum = pageNum;
        this.pageSize = pageSize;
        this.totalPages = pageSize > 0 ? (int) Math.ceil((double) total / pageSize) : 0;
    }

    public static <T> PageResult<T> of(List<T> list, long total, int pageNum, int pageSize) {
        return new PageResult<>(list, total, pageNum, pageSize);
    }
}
