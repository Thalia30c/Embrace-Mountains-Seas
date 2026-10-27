package com.cardgame.common;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 分页结果封装。
 *
 * @param <T> 记录类型
 */
public class PageResult<T> {

    private long total;
    private int page;
    private int size;
    private int totalPage;
    private List<T> records;

    public PageResult() {
        this.records = Collections.emptyList();
    }

    public PageResult(long total, int page, int size, List<T> records) {
        this.total = total;
        this.page = page <= 0 ? 1 : page;
        this.size = size <= 0 ? 10 : size;
        this.records = records == null ? new ArrayList<>() : records;
        this.totalPage = this.size == 0 ? 0 : (int) Math.ceil((double) total / this.size);
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

    public int getTotalPage() {
        return totalPage;
    }

    public void setTotalPage(int totalPage) {
        this.totalPage = totalPage;
    }

    public List<T> getRecords() {
        return records;
    }

    public void setRecords(List<T> records) {
        this.records = records;
    }
}
