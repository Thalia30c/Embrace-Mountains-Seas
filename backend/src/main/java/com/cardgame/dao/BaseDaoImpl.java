package com.cardgame.dao;

import com.cardgame.common.BizException;
import com.cardgame.common.PageResult;
import com.cardgame.common.ResultCode;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;

import java.lang.reflect.Field;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 统一数据库访问接口的默认实现，基于 Spring JdbcTemplate。
 *
 * <p>所有 SQL 都在这里统一拼装：表名、列名来自实体的 {@link Table}/{@link Column} 注解，
 * 查询条件来自 {@link QueryWrapper}，执行前会校验列名合法性。</p>
 *
 * @param <T> 实体类型
 */
public abstract class BaseDaoImpl<T> implements BaseDao<T> {

    protected final JdbcTemplate jdbcTemplate;

    /** 泛型实体类型，由子类通过泛型反射得到 */
    private final Class<T> entityClass;

    /** 表结构元数据缓存 */
    private TableInfo tableInfo;

    protected BaseDaoImpl(JdbcTemplate jdbcTemplate, Class<T> entityClass) {
        this.jdbcTemplate = jdbcTemplate;
        this.entityClass = entityClass;
    }

    /** 延迟解析表结构元数据，避免构造期反射异常 */
    protected TableInfo table() {
        if (tableInfo == null) {
            tableInfo = TableInfo.of(entityClass);
        }
        return tableInfo;
    }

    // ------------------------------------------------------------------ 写入

    @Override
    public int insert(T entity) {
        TableInfo info = table();
        List<TableInfo.ColumnInfo> columns = info.getInsertColumns();
        if (columns.isEmpty()) {
            return 0;
        }
        StringBuilder sql = new StringBuilder("INSERT INTO ").append(info.getTableName()).append(" (");
        List<Object> args = new ArrayList<>();
        for (int i = 0; i < columns.size(); i++) {
            if (i > 0) {
                sql.append(", ");
            }
            sql.append(columns.get(i).column).append(" = ?");
            args.add(readValue(columns.get(i), entity));
        }
        sql.append(")");

        if (info.isAutoIncrement() && info.getIdColumn() != null) {
            KeyHolder keyHolder = new GeneratedKeyHolder();
            int rows = jdbcTemplate.update(sql.toString(), keyHolder, args.toArray());
            if (rows > 0 && keyHolder.getKey() != null) {
                writeId(entity, keyHolder.getKey().longValue());
            }
            return rows;
        }
        return jdbcTemplate.update(sql.toString(), args.toArray());
    }

    @Override
    public int insertBatch(List<T> entities) {
        if (entities == null || entities.isEmpty()) {
            return 0;
        }
        TableInfo info = table();
        List<TableInfo.ColumnInfo> columns = info.getInsertColumns();
        if (columns.isEmpty()) {
            return 0;
        }
        StringBuilder sql = new StringBuilder("INSERT INTO ").append(info.getTableName()).append(" (");
        for (int i = 0; i < columns.size(); i++) {
            if (i > 0) {
                sql.append(", ");
            }
            sql.append(columns.get(i).column);
        }
        sql.append(") VALUES (");
        for (int i = 0; i < columns.size(); i++) {
            sql.append(i > 0 ? ", ?" : "?");
        }
        sql.append(")");

        List<Object[]> batchArgs = new ArrayList<>();
        for (T entity : entities) {
            Object[] row = new Object[columns.size()];
            for (int i = 0; i < columns.size(); i++) {
                row[i] = readValue(columns.get(i), entity);
            }
            batchArgs.add(row);
        }
        int[] rows = jdbcTemplate.batchUpdate(sql.toString(), batchArgs);
        int total = 0;
        for (int r : rows) {
            total += r;
        }
        return total;
    }

    @Override
    public int updateById(T entity) {
        TableInfo info = table();
        List<TableInfo.ColumnInfo> columns = info.getUpdateColumns();
        if (columns.isEmpty() || info.getIdColumn() == null) {
            return 0;
        }
        StringBuilder sql = new StringBuilder("UPDATE ").append(info.getTableName()).append(" SET ");
        List<Object> args = new ArrayList<>();
        for (int i = 0; i < columns.size(); i++) {
            if (i > 0) {
                sql.append(", ");
            }
            sql.append(columns.get(i).column).append(" = ?");
            args.add(readValue(columns.get(i), entity));
        }
        sql.append(" WHERE ").append(info.getIdColumn().column).append(" = ?");
        args.add(readValue(info.getIdColumn(), entity));
        return jdbcTemplate.update(sql.toString(), args.toArray());
    }

    @Override
    public int deleteById(Object id) {
        TableInfo info = table();
        if (info.getIdColumn() == null) {
            return 0;
        }
        String sql = "DELETE FROM " + info.getTableName() + " WHERE " + info.getIdColumn().column + " = ?";
        return jdbcTemplate.update(sql, id);
    }

    @Override
    public int deleteBy(QueryWrapper<T> wrapper) {
        TableInfo info = table();
        checkColumns(wrapper);
        String sql = "DELETE FROM " + info.getTableName() + wrapper.buildWhere();
        return jdbcTemplate.update(sql, wrapper.buildArgs().toArray());
    }

    // ------------------------------------------------------------------ 查询

    @Override
    public T selectById(Object id) {
        TableInfo info = table();
        if (info.getIdColumn() == null) {
            return null;
        }
        String sql = "SELECT " + info.allColumnsSql() + " FROM " + info.getTableName()
                + " WHERE " + info.getIdColumn().column + " = ?";
        List<T> list = jdbcTemplate.query(sql, rowMapper(entityClass), id);
        return list.isEmpty() ? null : list.get(0);
    }

    @Override
    public T selectOne(QueryWrapper<T> wrapper) {
        List<T> list = selectList(wrapper);
        return list.isEmpty() ? null : list.get(0);
    }

    @Override
    public List<T> selectList(QueryWrapper<T> wrapper) {
        TableInfo info = table();
        checkColumns(wrapper);
        StringBuilder sql = new StringBuilder("SELECT ").append(info.allColumnsSql())
                .append(" FROM ").append(info.getTableName())
                .append(wrapper.buildWhere())
                .append(wrapper.buildOrderBy());
        List<Object> args = wrapper.buildArgs();
        if (wrapper.hasLimit()) {
            sql.append(" LIMIT ? OFFSET ?");
            args.add(wrapper.limitSize());
            args.add(wrapper.limitOffset());
        }
        return jdbcTemplate.query(sql.toString(), rowMapper(entityClass), args.toArray());
    }

    @Override
    public long count(QueryWrapper<T> wrapper) {
        TableInfo info = table();
        checkColumns(wrapper);
        String sql = "SELECT COUNT(*) FROM " + info.getTableName() + wrapper.buildWhere();
        Long count = jdbcTemplate.queryForObject(sql, Long.class, wrapper.buildArgs().toArray());
        return count == null ? 0L : count;
    }

    @Override
    public PageResult<T> selectPage(QueryWrapper<T> wrapper, int page, int size) {
        long total = count(wrapper);
        int current = page <= 0 ? 1 : page;
        int pageSize = size <= 0 ? 10 : Math.min(size, 100);
        List<T> records = selectList(wrapper.limit((current - 1) * pageSize, pageSize));
        return new PageResult<>(total, current, pageSize, records);
    }

    @Override
    public List<T> selectBySql(String sql, Class<T> clazz, Object... args) {
        return jdbcTemplate.query(sql, rowMapper(clazz), args);
    }

    // ------------------------------------------------------------------ 内部工具

    /**
     * 校验 QueryWrapper 中用到的列名是否在实体中定义，防止 SQL 注入。
     */
    private void checkColumns(QueryWrapper<T> wrapper) {
        TableInfo info = table();
        for (String col : wrapper.getConditionColumns()) {
            if (info.getColumn(col) == null) {
                throw new BizException(ResultCode.PARAM_ERROR, "非法的查询列: " + col);
            }
        }
        for (String col : wrapper.getOrderByColumns()) {
            if (info.getColumn(col) == null) {
                throw new BizException(ResultCode.PARAM_ERROR, "非法的排序列: " + col);
            }
        }
    }

    private Object readValue(TableInfo.ColumnInfo ci, T entity) {
        try {
            Object v = ci.field.get(entity);
            if (v instanceof LocalDateTime) {
                return Timestamp.valueOf((LocalDateTime) v);
            }
            return v;
        } catch (IllegalAccessException e) {
            throw new BizException(ResultCode.SERVER_ERROR, "读取实体字段失败: " + ci.field.getName());
        }
    }

    private void writeId(T entity, long id) {
        TableInfo.ColumnInfo idColumn = table().getIdColumn();
        if (idColumn == null) {
            return;
        }
        try {
            Field f = idColumn.field;
            Class<?> type = f.getType();
            if (type == Long.class || type == long.class) {
                f.set(entity, id);
            } else if (type == Integer.class || type == int.class) {
                f.set(entity, (int) id);
            }
        } catch (IllegalAccessException ignored) {
            // 主键回填失败不影响主流程
        }
    }

    /**
     * 通用行映射：按列名匹配实体字段（下划线转驼峰）。
     */
    public static <E> RowMapper<E> rowMapper(Class<E> clazz) {
        return (rs, rowNum) -> {
            E entity;
            try {
                entity = clazz.getDeclaredConstructor().newInstance();
            } catch (Exception e) {
                throw new SQLException("实例化实体失败: " + clazz.getName(), e);
            }
            TableInfo info = TableInfo.of(clazz);
            for (TableInfo.ColumnInfo ci : info.getColumns()) {
                try {
                    Object value = rs.getObject(ci.column);
                    if (value == null) {
                        continue;
                    }
                    setFieldValue(ci.field, entity, value);
                } catch (SQLException ignored) {
                    // 某列在结果集中不存在时跳过
                }
            }
            return entity;
        };
    }

    private static void setFieldValue(Field field, Object entity, Object value) {
        try {
            field.setAccessible(true);
            Class<?> type = field.getType();
            if (type == String.class) {
                field.set(entity, value.toString());
            } else if (type == Long.class || type == long.class) {
                field.set(entity, ((Number) value).longValue());
            } else if (type == Integer.class || type == int.class) {
                field.set(entity, ((Number) value).intValue());
            } else if (type == Double.class || type == double.class) {
                field.set(entity, ((Number) value).doubleValue());
            } else if (type == Float.class || type == float.class) {
                field.set(entity, ((Number) value).floatValue());
            } else if (type == Boolean.class || type == boolean.class) {
                field.set(entity, value instanceof Boolean ? value : ((Number) value).intValue() != 0);
            } else if (type == LocalDateTime.class) {
                if (value instanceof Timestamp) {
                    field.set(entity, ((Timestamp) value).toLocalDateTime());
                } else if (value instanceof java.util.Date) {
                    field.set(entity, ((java.util.Date) value).toInstant()
                            .atZone(java.time.ZoneId.systemDefault()).toLocalDateTime());
                }
            } else if (type == java.util.Date.class) {
                field.set(entity, value instanceof java.util.Date ? value : new java.util.Date());
            } else {
                field.set(entity, value);
            }
        } catch (IllegalAccessException ignored) {
            // 字段类型不匹配时跳过，不影响其它字段
        }
    }

    /** 供子类扩展：获取当前实体类 */
    protected Class<T> getEntityClass() {
        return entityClass;
    }

    /** 供子类扩展：直接执行查询并映射 */
    protected List<Map<String, Object>> queryMaps(String sql, Object... args) {
        return jdbcTemplate.queryForList(sql, args);
    }
}
