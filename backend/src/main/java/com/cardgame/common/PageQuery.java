package com.cardgame.common;

/**
 * 分页请求基类，Controller 可直接继承。
 */
public class PageQuery {

    private int page = 1;  // 页码，从 1 开始
    private int size = 10; // 每页条数

    public int getPage() {
        return page <= 0 ? 1 : page;
    }

    public void setPage(int page) {
        this.page = page;
    }

    public int getSize() {
        if (size <= 0) {
            return 10;
        }
        return Math.min(size, 100); // 单页最多 100 条，防止拉爆数据库
    }

    public void setSize(int size) {
        this.size = size;
    }

    /** 计算 SQL 的 OFFSET */
    public int getOffset() {
        return (getPage() - 1) * getSize();
    }
}
