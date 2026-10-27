package com.cardgame.dao;

import com.cardgame.common.PageResult;

import java.util.List;

/**
 * 通用数据访问接口（DAO 层统一出口）。
 *
 * <p>约定：</p>
 * <ul>
 *     <li>实体通过 {@link Table} / {@link Column} 注解声明表名与列名，通用 SQL 由实现类拼装；</li>
 *     <li>方法中的 column 参数只允许传实体声明过的列名，非法列名会被拒绝，避免 SQL 注入；</li>
 *     <li>Service 层不再直接使用 JdbcTemplate，统一调用本接口及其子接口；</li>
 *     <li>复杂或多表查询写在子接口中，用 {@link #selectBySql(String, Object...)} 执行，参数一律用 ? 占位。</li>
 * </ul>
 *
 * @param <T> 实体类型
 */
public interface BaseDao<T> {

    /**
     * 新增一条记录，自增主键会回填到实体对象。
     *
     * @return 受影响行数
     */
    int insert(T entity);

    /**
     * 按主键更新非空 T 实体的全部普通列。
     */
    int updateById(T entity);

    /**
     * 按主键删除。
     */
    int deleteById(Object id);

    /**
     * 按主键查询，查不到返回 null。
     */
    T selectById(Object id);

    /**
     * 查询全表。
     */
    List<T> selectAll();

    /**
     * 查询全表并按 orderBy 排序，如 selectAll("rankId DESC")。
     */
    List<T> selectAll(String orderBy);

    /**
     * 按列等值查询列表。
     */
    List<T> selectList(String column, Object value);

    /**
     * 按列等值查询列表，带排序与条数限制，limit <= 0 表示不限制。
     */
    List<T> selectList(String column, Object value, String orderBy, int limit);

    /**
     * 按列等值查询单条，多条时取第一条。
     */
    T selectOne(String column, Object value);

    /**
     * 统计总条数。
     */
    long count();

    /**
     * 按列等值统计条数。
     */
    long count(String column, Object value);

    /**
     * 分页查询全表。
     */
    PageResult<T> selectPage(int page, int size);

    /**
     * 按列等值分页查询。
     */
    PageResult<T> selectPage(String column, Object value, int page, int size);

    /**
     * 原生 SQL 查询，结果按列名映射回实体，参数用 ? 占位。
     */
    List<T> selectBySql(String sql, Object... args);

    /**
     * 原生 SQL 执行更新/删除，返回受影响行数。
     */
    int execute(String sql, Object... args);
}
