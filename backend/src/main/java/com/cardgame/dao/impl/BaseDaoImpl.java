package com.cardgame.dao.impl;

import com.cardgame.common.PageResult;
import com.cardgame.dao.BaseDao;
import com.cardgame.dao.TableInfo;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * {@link BaseDao} 的通用实现，基于 Spring JdbcTemplate。
 *
 * <p>各表的 DAO 实现类只需继承本类并传入实体类型，即拥有增删改查、分页、原生 SQL 的能力，
 * 特有业务查询在子类里继续扩展即可。</p>
 *
 * @param <T> 实体类型
 */
public abstract class BaseDaoImpl<T> implements BaseDao<T> {

    protected final JdbcTemplate jdbcTemplate;

    private final Class<T> entityClass;

    /** 表结构元数据，延迟解析 */
    private volatile TableInfo meta;

    protected BaseDaoImpl(JdbcTemplate jdbcTemplate, Class<T> entityClass) {
        if (jdbcTemplate == null) {
            throw new IllegalArgumentException("JdbcTemplate 不能为空");
        }
        this.jdbcTemplate = jdbcTemplate;
        this.entityClass = entityClass;
    }

    /** 当前实体对应的表元数据 */
    protected TableInfo meta() {
        TableInfo info = meta;
        if (info == null) {
            synchronized (this) {
                info = meta;
                if (info == null) {
                    info = TableInfo.of(entityClass);
                    meta = info;
                }
            }
        }
        return info;
    }

    /** 当前表名，子类写扩展 SQL 时使用 */
    protected String table() {
        return meta().getTableName();
    }

    // ------------------------------------------------------------------ 写入

    @Override
    public int insert(T entity) {
        if (entity == null) {
            return 0;
        }
        TableInfo info = meta();
        List<TableInfo.ColumnMeta> columns = info.getInsertColumns();
        StringBuilder sql = new StringBuilder("INSERT INTO ").append(table()).append(" (");
        for (int i = 0; i < columns.size(); i++) {
            sql.append(i > 0 ? ", " : "").append('`').append(columns.get(i).getColumn()).append('`');
        }
        sql.append(") VALUES (");
        for (int i = 0; i < columns.size(); i++) {
            sql.append(i > 0 ? ", ?" : "?");
        }
        sql.append(')');
        Object[] args = readValues(columns, entity);

        TableInfo.ColumnMeta idColumn = info.getIdColumn();
        if (info.isAutoIncrement() && idColumn != null) {
            KeyHolder keyHolder = new GeneratedKeyHolder();
            int rows = jdbcTemplate.update(con -> {
                PreparedStatement ps = con.prepareStatement(sql.toString(), Statement.RETURN_GENERATED_KEYS);
                for (int i = 0; i < args.length; i++) {
                    ps.setObject(i + 1, args[i]);
                }
                return ps;
            }, keyHolder);
            writeGeneratedId(entity, idColumn, keyHolder);
            return rows;
        }
        return jdbcTemplate.update(sql.toString(), args);
    }

    @Override
    public int updateById(T entity) {
        if (entity == null) {
            return 0;
        }
        TableInfo info = meta();
        TableInfo.ColumnMeta idColumn = info.getIdColumn();
        if (idColumn == null) {
            throw new IllegalStateException(table() + " 未声明主键，无法按主键更新");
        }
        List<TableInfo.ColumnMeta> columns = info.getUpdateColumns();
        StringBuilder sql = new StringBuilder("UPDATE ").append(table()).append(" SET ");
        for (int i = 0; i < columns.size(); i++) {
            sql.append(i > 0 ? ", " : "").append('`').append(columns.get(i).getColumn()).append("` = ?");
        }
        sql.append(" WHERE `").append(idColumn.getColumn()).append("` = ?");

        List<Object> args = new ArrayList<>(columns.size() + 1);
        for (TableInfo.ColumnMeta c : columns) {
            args.add(c.read(entity));
        }
        args.add(idColumn.read(entity));
        return jdbcTemplate.update(sql.toString(), args.toArray());
    }

    @Override
    public int deleteById(Object id) {
        TableInfo.ColumnMeta idColumn = meta().getIdColumn();
        if (idColumn == null) {
            throw new IllegalStateException(table() + " 未声明主键，无法按主键删除");
        }
        return jdbcTemplate.update("DELETE FROM " + table()
                + " WHERE `" + idColumn.getColumn() + "` = ?", id);
    }

    @Override
    public int execute(String sql, Object... args) {
        return jdbcTemplate.update(sql, args);
    }

    // ------------------------------------------------------------------ 查询

    @Override
    public T selectById(Object id) {
        TableInfo.ColumnMeta idColumn = meta().getIdColumn();
        if (idColumn == null) {
            throw new IllegalStateException(table() + " 未声明主键，无法按主键查询");
        }
        return selectOne(idColumn.getColumn(), id);
    }

    @Override
    public List<T> selectAll() {
        return selectListBy(null, null, null, 0);
    }

    @Override
    public List<T> selectAll(String orderBy) {
        return selectListBy(null, null, orderBy, 0);
    }

    @Override
    public List<T> selectList(String column, Object value) {
        return selectListBy(column, value, null, 0);
    }

    @Override
    public List<T> selectList(String column, Object value, String orderBy, int limit) {
        return selectListBy(column, value, orderBy, limit);
    }

    @Override
    public T selectOne(String column, Object value) {
        List<T> list = selectListBy(column, value, null, 1);
        return list.isEmpty() ? null : list.get(0);
    }

    @Override
    public long count() {
        Long total = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM " + table(), Long.class);
        return total == null ? 0L : total;
    }

    @Override
    public long count(String column, Object value) {
        meta().require(column);
        Long total = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM " + table() + " WHERE `" + column.trim() + "` = ?", Long.class, value);
        return total == null ? 0L : total;
    }

    @Override
    public PageResult<T> selectPage(int page, int size) {
        return page(null, null, page, size);
    }

    @Override
    public PageResult<T> selectPage(String column, Object value, int page, int size) {
        return page(column, value, page, size);
    }

    @Override
    public List<T> selectBySql(String sql, Object... args) {
        return jdbcTemplate.query(sql, rowMapper(entityClass), args);
    }

    // ------------------------------------------------------------------ 子类可用工具

    /** 按排序取前 limit 条，limit <= 0 时取全部 */
    protected List<T> selectTop(String orderBy, int limit) {
        return selectListBy(null, null, orderBy, limit);
    }

    /** 执行查询并返回 Map 列表，用于聚合统计、跨表联查等非实体结果 */
    protected List<Map<String, Object>> selectMaps(String sql, Object... args) {
        return jdbcTemplate.queryForList(sql, args);
    }

    /** 执行查询并返回单个值 */
    protected <V> V selectObject(String sql, Class<V> type, Object... args) {
        return jdbcTemplate.queryForObject(sql, type, args);
    }

    /** 通用行映射器：按列名匹配实体字段 */
    protected <E> RowMapper<E> rowMapper(Class<E> clazz) {
        return (rs, rowNum) -> mapRow(rs, clazz);
    }

    // ------------------------------------------------------------------ 内部实现

    private PageResult<T> page(String column, Object value, int page, int size) {
        int current = page <= 0 ? 1 : page;
        int pageSize = size <= 0 ? 10 : Math.min(size, 200);
        long total = column == null ? count() : count(column, value);
        List<T> records = jdbcTemplate.query(
                buildSelect(column, null, (pageSize == 0 ? "" : " LIMIT " + pageSize + " OFFSET " + (current - 1) * pageSize)),
                rowMapper(entityClass),
                column == null ? new Object[0] : new Object[]{value});
        return new PageResult<>(total, current, pageSize, records);
    }

    private List<T> selectListBy(String column, Object value, String orderBy, int limit) {
        StringBuilder sql = new StringBuilder(buildSelect(column, orderBy,
                limit > 0 ? " LIMIT " + limit : ""));
        return jdbcTemplate.query(sql.toString(), rowMapper(entityClass),
                column == null ? new Object[0] : new Object[]{value});
    }

    /**
     * 组装 SELECT 语句，列名与排序列都经过 {@link TableInfo#require(String)} 校验。
     */
    private String buildSelect(String column, String orderBy, String tail) {
        TableInfo info = meta();
        StringBuilder sql = new StringBuilder("SELECT ").append(info.allColumnsSql()).append(" FROM ").append(table());
        if (column != null && !column.trim().isEmpty()) {
            info.require(column);
            sql.append(" WHERE `").append(column.trim()).append("` = ?");
        }
        sql.append(TableInfo.parseOrderBy(info, orderBy));
        sql.append(tail);
        return sql.toString();
    }

    private Object[] readValues(List<TableInfo.ColumnMeta> columns, T entity) {
        Object[] args = new Object[columns.size()];
        for (int i = 0; i < columns.size(); i++) {
            Object value = columns.get(i).read(entity);
            args[i] = value instanceof LocalDateTime ? Timestamp.valueOf((LocalDateTime) value) : value;
        }
        return args;
    }

    private void writeGeneratedId(T entity, TableInfo.ColumnMeta idColumn, KeyHolder keyHolder) {
        Number key = keyHolder.getKey();
        if (key == null) {
            return;
        }
        Class<?> type = idColumn.getField().getType();
        if (type == Long.class || type == long.class) {
            idColumn.write(entity, key.longValue());
        } else if (type == Integer.class || type == int.class) {
            idColumn.write(entity, key.intValue());
        } else if (type == String.class) {
            idColumn.write(entity, String.valueOf(key));
        }
    }

    private <E> E mapRow(ResultSet rs, Class<E> clazz) throws SQLException {
        try {
            E entity = clazz.getDeclaredConstructor().newInstance();
            for (TableInfo.ColumnMeta column : meta().getColumns()) {
                column.write(entity, convert(rs.getObject(column.getColumn()), column.getField().getType()));
            }
            return entity;
        } catch (SQLException e) {
            throw e;
        } catch (Exception e) {
            throw new SQLException("实例化实体失败: " + clazz.getName(), e);
        }
    }

    /** JDBC 值转实体字段类型 */
    private Object convert(Object value, Class<?> type) {
        if (value == null) {
            return null;
        }
        if (type == String.class) {
            return value.toString();
        }
        if (type == LocalDateTime.class) {
            if (value instanceof Timestamp) {
                return ((Timestamp) value).toLocalDateTime();
            }
            if (value instanceof Date) {
                return LocalDateTime.ofInstant(((Date) value).toInstant(), java.time.ZoneId.systemDefault());
            }
        }
        if (value instanceof Number) {
            Number num = (Number) value;
            if (type == Integer.class || type == int.class) {
                return num.intValue();
            }
            if (type == Long.class || type == long.class) {
                return num.longValue();
            }
            if (type == Short.class || type == short.class) {
                return num.shortValue();
            }
            if (type == Byte.class || type == byte.class) {
                return num.byteValue();
            }
            if (type == Double.class || type == double.class) {
                return num.doubleValue();
            }
            if (type == Float.class || type == float.class) {
                return num.floatValue();
            }
        }
        if ((type == Boolean.class || type == boolean.class) && value instanceof Number) {
            return ((Number) value).intValue() != 0;
        }
        return value;
    }
}
