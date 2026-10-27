package com.cardgame.dao;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 由 {@link Table} / {@link Column} 注解解析出的表结构元数据，一个类只解析一次并缓存。
 *
 * <p>所有 SQL 的表名、列名都从这里取，外部传入的列名必须先经 {@link #require(String)} 校验，
 * 保证列名只能是实体里声明过的列，杜绝字符串拼接带来的 SQL 注入。</p>
 */
public final class TableInfo {

    private static final Map<Class<?>, TableInfo> CACHE = new ConcurrentHashMap<>();

    private final Class<?> entityClass;
    private final String tableName;
    private final boolean autoIncrement;
    private final Map<String, ColumnMeta> columns = new LinkedHashMap<>();
    private final ColumnMeta idColumn;

    private TableInfo(Class<?> entityClass) {
        this.entityClass = entityClass;
        Table table = entityClass.getAnnotation(Table.class);
        if (table == null) {
            throw new IllegalArgumentException("实体缺少 @Table 注解: " + entityClass.getName());
        }
        this.tableName = "`" + table.value() + "`";
        this.autoIncrement = table.autoIncrement();

        ColumnMeta id = null;
        for (Class<?> c = entityClass; c != null && c != Object.class; c = c.getSuperclass()) {
            for (Field field : c.getDeclaredFields()) {
                Column column = field.getAnnotation(Column.class);
                if (column == null || "serialVersionUID".equals(field.getName())) {
                    continue;
                }
                String name = column.value().isEmpty()
                        ? camelToUnderscore(field.getName())
                        : column.value();
                field.setAccessible(true);
                ColumnMeta meta = new ColumnMeta(field, name, column.id());
                this.columns.put(name, meta);
                if (column.id()) {
                    id = meta;
                }
            }
        }
        if (this.columns.isEmpty()) {
            throw new IllegalArgumentException("实体没有任何 @Column 字段: " + entityClass.getName());
        }
        this.idColumn = id;
    }

    /** 获取（并缓存）实体对应的表元数据 */
    public static TableInfo of(Class<?> entityClass) {
        return CACHE.computeIfAbsent(entityClass, TableInfo::new);
    }

    public Class<?> getEntityClass() {
        return entityClass;
    }

    /** 带反引号的表名，可直接拼进 SQL */
    public String getTableName() {
        return tableName;
    }

    public boolean isAutoIncrement() {
        return autoIncrement;
    }

    public Collection<ColumnMeta> getColumns() {
        return columns.values();
    }

    public ColumnMeta getIdColumn() {
        return idColumn;
    }

    /** insert 使用的列，自增主键不参与 */
    public List<ColumnMeta> getInsertColumns() {
        List<ColumnMeta> list = new ArrayList<>(columns.size());
        for (ColumnMeta meta : columns.values()) {
            if (autoIncrement && meta.getId()) {
                continue;
            }
            list.add(meta);
        }
        return list;
    }

    /** update 使用的列，主键不参与 */
    public List<ColumnMeta> getUpdateColumns() {
        List<ColumnMeta> list = new ArrayList<>(columns.size());
        for (ColumnMeta meta : columns.values()) {
            if (meta.getId()) {
                continue;
            }
            list.add(meta);
        }
        return list;
    }

    /** 全部列名拼成的 SELECT 片段，如 `uid`, `username` */
    public String allColumnsSql() {
        StringBuilder sb = new StringBuilder();
        for (ColumnMeta meta : columns.values()) {
            if (sb.length() > 0) {
                sb.append(", ");
            }
            sb.append('`').append(meta.getColumn()).append('`');
        }
        return sb.toString();
    }

    /** 按列名取列信息，不存在返回 null */
    public ColumnMeta get(String column) {
        return column == null ? null : columns.get(column.trim());
    }

    /** 按列名取列信息，不存在抛异常，用于校验外部传入的列名 */
    public ColumnMeta require(String column) {
        ColumnMeta meta = get(column);
        if (meta == null) {
            throw new IllegalArgumentException("非法的列名: " + column + "（表 " + tableName + "）");
        }
        return meta;
    }

    /** 字段名驼峰转下划线 */
    public static String camelToUnderscore(String name) {
        StringBuilder sb = new StringBuilder(name.length() + 4);
        for (int i = 0; i < name.length(); i++) {
            char ch = name.charAt(i);
            if (Character.isUpperCase(ch)) {
                sb.append('_').append(Character.toLowerCase(ch));
            } else {
                sb.append(ch);
            }
        }
        return sb.toString();
    }

    /** 单列元数据 */
    public static final class ColumnMeta {

        private final Field field;
        private final String column;
        private final boolean id;

        ColumnMeta(Field field, String column, boolean id) {
            this.field = field;
            this.column = column;
            this.id = id;
        }

        public Field getField() {
            return field;
        }

        public String getColumn() {
            return column;
        }

        public boolean getId() {
            return id;
        }

        /** 读取实体上该字段的值 */
        public Object read(Object entity) {
            try {
                return field.get(entity);
            } catch (IllegalAccessException e) {
                throw new IllegalStateException("读取字段失败: " + field.getName(), e);
            }
        }

        /** 写入实体该字段 */
        public void write(Object entity, Object value) {
            try {
                field.set(entity, value);
            } catch (IllegalAccessException e) {
                throw new IllegalStateException("写入字段失败: " + field.getName(), e);
            }
        }

        @Override
        public String toString() {
            return column + " -> " + field.getName();
        }
    }

    /** 解析排序串 "列名 ASC|DESC"，校验列名与方向合法性 */
    public static String parseOrderBy(TableInfo info, String orderBy) {
        if (orderBy == null || orderBy.trim().isEmpty()) {
            return "";
        }
        String[] parts = orderBy.trim().split("\\s+");
        String column = parts[0];
        info.require(column);
        String direction = "";
        if (parts.length > 1) {
            String dir = parts[1].toUpperCase(Locale.ROOT);
            if (!"ASC".equals(dir) && !"DESC".equals(dir)) {
                throw new IllegalArgumentException("非法的排序方向: " + parts[1]);
            }
            direction = " " + dir;
        }
        return " ORDER BY `" + column + "`" + direction;
    }
}
