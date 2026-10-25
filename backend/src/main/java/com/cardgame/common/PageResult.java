package com.cardgame.common;

import java.util.List;

/**
 * 分页结果封装。
 *
 * @param <T> 列表元素类型
 */
public class PageResult<T> {

    private long total;      // 总记录数
    private int page;        // 当前页码，从 1 开始
    private int size;        // 每页条数
    private List<T> records; // 当前页数据

    public PageResult() {
    }

    public PageResult(long total, int page, int size, List<T> records) {
        this.total = total;
        this.page = page;
        this.size = size;
        this.records = records;
    }

    public long getTotal() {
        return total;
    }

    public void setTotal(long total) {
        this.total = total;
    }

    public int getPage() {
        return page;
    }

    public void setPage(int page) {
        this.page = page;
    }

    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
    }

    public List<T> getRecords() {
        return records;
    }

    public void setRecords(List<T> records) {
        this.records = records;
    }
}
