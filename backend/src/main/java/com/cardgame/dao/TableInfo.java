package com.cardgame.dao;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 实体类的表结构元数据，构造时反射解析一次并缓存。
 */
public class TableInfo {

    /** 单个字段的映射信息 */
    public static class ColumnInfo {
        public final Field field;
        public final String column;
        public final boolean id;
        public final boolean insertable;
        public final boolean updatable;

        ColumnInfo(Field field, String column, boolean id, boolean insertable, boolean updatable) {
            this.field = field;
            this.column = column;
            this.id = id;
            this.insertable = insertable;
            this.updatable = updatable;
        }
    }

    private final Class<?> entityClass;
    private final String tableName;
    private final boolean autoIncrement;
    private final ColumnInfo idColumn;
    private final List<ColumnInfo> columns;
    private final Map<String, ColumnInfo> columnIndex;

    private TableInfo(Class<?> entityClass, String tableName, boolean autoIncrement,
                      ColumnInfo idColumn, List<ColumnInfo> columns) {
        this.entityClass = entityClass;
        this.tableName = tableName;
        this.autoIncrement = autoIncrement;
        this.idColumn = idColumn;
        this.columns = columns;
        Map<String, ColumnInfo> index = new LinkedHashMap<>();
        for (ColumnInfo c : columns) {
            index.put(c.column.toLowerCase(), c);
        }
        this.columnIndex = index;
    }

    /**
     * 解析实体类，得到表结构元数据。
     */
    public static TableInfo of(Class<?> entityClass) {
        Table table = entityClass.getAnnotation(Table.class);
        String tableName;
        boolean autoIncrement = true;
        if (table != null) {
            tableName = table.value();
            autoIncrement = table.autoIncrement();
        } else {
            tableName = "t_" + camelToUnderline(entityClass.getSimpleName());
        }

        List<ColumnInfo> columns = new ArrayList<>();
        ColumnInfo idColumn = null;
        for (Field field : entityClass.getDeclaredFields()) {
            if (field.isSynthetic() || java.lang.reflect.Modifier.isStatic(field.getModifiers())) {
                continue;
            }
            Column anno = field.getAnnotation(Column.class);
            String columnName = (anno != null && !anno.value().isEmpty())
                    ? anno.value()
                    : camelToUnderline(field.getName());
            boolean isId = anno != null && anno.id();
            boolean insertable = anno == null || anno.insertable();
            boolean updatable = anno == null || anno.updatable();
            field.setAccessible(true);
            ColumnInfo info = new ColumnInfo(field, columnName, isId, insertable, updatable);
            columns.add(info);
            if (isId) {
                idColumn = info;
            }
        }
        if (idColumn == null && !columns.isEmpty()) {
            // 未显式标注主键时，取第一个字段作为主键
            ColumnInfo first = columns.get(0);
            idColumn = new ColumnInfo(first.field, first.column, true, first.insertable, first.updatable);
            columns.set(0, idColumn);
        }
        return new TableInfo(entityClass, tableName, autoIncrement, idColumn, columns);
    }

    /** 驼峰转下划线 */
    public static String camelToUnderline(String text) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            if (Character.isUpperCase(c) && i > 0) {
                sb.append('_');
            }
            sb.append(Character.toLowerCase(c));
        }
        return sb.toString();
    }

    public Class<?> getEntityClass() {
        return entityClass;
    }

    public String getTableName() {
        return tableName;
    }

    public boolean isAutoIncrement() {
        return autoIncrement;
    }

    public ColumnInfo getIdColumn() {
        return idColumn;
    }

    public List<ColumnInfo> getColumns() {
        return columns;
    }

    /** 按列名查找，找不到返回 null */
    public ColumnInfo getColumn(String column) {
        return columnIndex.get(column.toLowerCase());
    }

    /** 取出参与插入的列 */
    public List<ColumnInfo> getInsertColumns() {
        List<ColumnInfo> list = new ArrayList<>();
        for (ColumnInfo c : columns) {
            if (c.insertable && !(c.id && autoIncrement)) {
                list.add(c);
            }
        }
        return list;
    }

    /** 取出参与更新的列 */
    public List<ColumnInfo> getUpdateColumns() {
        List<ColumnInfo> list = new ArrayList<>();
        for (ColumnInfo c : columns) {
            if (c.updatable && !c.id) {
                list.add(c);
            }
        }
        return list;
    }

    /** 全部列名，逗号分隔 */
    public String allColumnsSql() {
        StringBuilder sb = new StringBuilder();
        for (ColumnInfo c : columns) {
            if (sb.length() > 0) {
                sb.append(", ");
            }
            sb.append(c.column);
        }
        return sb.toString();
    }
}
