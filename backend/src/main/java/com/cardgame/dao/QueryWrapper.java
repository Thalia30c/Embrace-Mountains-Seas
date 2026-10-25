package com.cardgame.dao;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 查询条件构造器（链式 API）。
 *
 * <pre>
 * QueryWrapper&lt;User&gt; qw = QueryWrapper.create();
 * qw.eq("status", 1).like("nickname", "张").orderByDesc("create_time").limit(10, 20);
 * </pre>
 *
 * <p>列名在执行前会与 {@link TableInfo} 校验，未定义的列会直接抛异常，
 * 从根本上避免 SQL 注入。</p>
 */
public class QueryWrapper<T> {

    /** 单个查询条件 */
    public static class Condition {
        final String column;
        final String operator;
        final Object value;
        final boolean isOr;

        Condition(String column, String operator, Object value, boolean isOr) {
            this.column = column;
            this.operator = operator;
            this.value = value;
            this.isOr = isOr;
        }

        /** 列名 */
        public String getColumn() {
            return column;
        }

        /** 比较运算符：= &lt;&gt; &gt; &gt;= &lt; &lt;= LIKE IN IS NULL IS NOT NULL */
        public String getOperator() {
            return operator;
        }

        /** 比较值 */
        public Object getValue() {
            return value;
        }

        /** 是否为 OR 关系 */
        public boolean isOr() {
            return isOr;
        }
    }

    private final List<Condition> conditions = new ArrayList<>();
    private final Map<String, Object> params = new LinkedHashMap<>();
    private final List<String> orderBy = new ArrayList<>();
    private int paramIndex = 0;

    public static <T> QueryWrapper<T> create() {
        return new QueryWrapper<>();
    }

    /** 等于 */
    public QueryWrapper<T> eq(String column, Object value) {
        return add(column, "=", value);
    }

    /** 不等于 */
    public QueryWrapper<T> ne(String column, Object value) {
        return add(column, "<>", value);
    }

    /** 大于 */
    public QueryWrapper<T> gt(String column, Object value) {
        return add(column, ">", value);
    }

    /** 大于等于 */
    public QueryWrapper<T> ge(String column, Object value) {
        return add(column, ">=", value);
    }

    /** 小于 */
    public QueryWrapper<T> lt(String column, Object value) {
        return add(column, "<", value);
    }

    /** 小于等于 */
    public QueryWrapper<T> le(String column, Object value) {
        return add(column, "<=", value);
    }

    /** 模糊匹配 */
    public QueryWrapper<T> like(String column, String value) {
        return add(column, "LIKE", "%" + value + "%");
    }

    /** 前缀匹配 */
    public QueryWrapper<T> likeRight(String column, String value) {
        return add(column, "LIKE", value + "%");
    }

    /** IN 查询 */
    public QueryWrapper<T> in(String column, Collection<?> values) {
        if (values == null || values.isEmpty()) {
            // 空集合直接拼一个恒假条件，避免生成非法 SQL
            return add(column, "=", -1);
        }
        return add(column, "IN", values);
    }

    /** IS NULL */
    public QueryWrapper<T> isNull(String column) {
        return add(column, "IS NULL", null);
    }

    /** IS NOT NULL */
    public QueryWrapper<T> isNotNull(String column) {
        return add(column, "IS NOT NULL", null);
    }

    /** OR 关系的等值条件 */
    public QueryWrapper<T> orEq(String column, Object value) {
        conditions.add(new Condition(column, "=", value, true));
        return this;
    }

    private QueryWrapper<T> add(String column, String operator, Object value) {
        conditions.add(new Condition(column, operator, value, false));
        return this;
    }

    /** 升序排序 */
    public QueryWrapper<T> orderByAsc(String column) {
        orderBy.add(column + " ASC");
        return this;
    }

    /** 降序排序 */
    public QueryWrapper<T> orderByDesc(String column) {
        orderBy.add(column + " DESC");
        return this;
    }

    /** 分页 */
    public QueryWrapper<T> limit(int offset, int size) {
        params.put("__limitSize", size);
        params.put("__limitOffset", offset);
        return this;
    }

    /** 仅限制条数 */
    public QueryWrapper<T> limit(int size) {
        params.put("__limitSize", size);
        return this;
    }

    /** 生成 WHERE 子句（不含 WHERE 关键字），无条件时返回空串 */
    public String buildWhere() {
        if (conditions.isEmpty()) {
            return "";
        }
        StringBuilder sb = new StringBuilder(" WHERE ");
        boolean first = true;
        for (Condition c : conditions) {
            if (!first) {
                sb.append(c.isOr ? " OR " : " AND ");
            }
            first = false;
            if ("IS NULL".equals(c.operator)) {
                sb.append(c.column).append(" IS NULL");
            } else if ("IS NOT NULL".equals(c.operator)) {
                sb.append(c.column).append(" IS NOT NULL");
            } else if ("IN".equals(c.operator)) {
                sb.append(c.column).append(" IN (");
                Collection<?> values = (Collection<?>) c.value;
                int i = 0;
                for (Object v : values) {
                    if (i > 0) {
                        sb.append(", ");
                    }
                    sb.append("?").append(paramIndex++);
                    params.put("p" + (paramIndex - 1), v);
                    i++;
                }
                sb.append(")");
            } else {
                sb.append(c.column).append(" ").append(c.operator).append(" ?").append(paramIndex++);
                params.put("p" + (paramIndex - 1), c.value);
            }
        }
        return sb.toString();
    }

    /** 生成 ORDER BY 子句 */
    public String buildOrderBy() {
        if (orderBy.isEmpty()) {
            return "";
        }
        return " ORDER BY " + String.join(", ", orderBy);
    }

    /** 取出占位符对应的参数，顺序与 SQL 中 ? 一致 */
    public List<Object> buildArgs() {
        List<Object> args = new ArrayList<>();
        for (int i = 0; i < paramIndex; i++) {
            args.add(params.get("p" + i));
        }
        return args;
    }

    public boolean hasLimit() {
        return params.containsKey("__limitSize");
    }

    public int limitOffset() {
        Object v = params.get("__limitOffset");
        return v == null ? 0 : (Integer) v;
    }

    public int limitSize() {
        Object v = params.get("__limitSize");
        return v == null ? 0 : (Integer) v;
    }

    public List<String> getOrderByColumns() {
        List<String> list = new ArrayList<>();
        for (String s : orderBy) {
            list.add(s.split(" ")[0]);
        }
        return list;
    }

    /** 取出所有被用于条件的列名，用于安全校验 */
    public List<String> getConditionColumns() {
        List<String> list = new ArrayList<>();
        for (Condition c : conditions) {
            list.add(c.column);
        }
        return list;
    }

    /** 取出全部查询条件，供内存版 DAO 做内存过滤 */
    public List<Condition> getConditions() {
        return new ArrayList<>(conditions);
    }

    /** 取出排序列及其方向，格式形如 "win_count DESC" */
    public List<String> getOrderByEntries() {
        return new ArrayList<>(orderBy);
    }
}
