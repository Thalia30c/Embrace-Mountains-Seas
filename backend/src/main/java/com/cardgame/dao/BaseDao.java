package com.cardgame.dao;

import com.cardgame.common.PageResult;
import java.util.List;

/**
 * 统一数据库访问接口（全项目唯一的对外数据访问出口）。
 *
 * <p>设计约定：</p>
 * <ul>
 *     <li>Service 层禁止直接使用 JdbcTemplate，所有持久化操作必须经过本接口。</li>
 *     <li>本接口只提供通用的增删改查能力，各业务 DAO 通过继承扩展出特有的查询方法。</li>
 *     <li>条件构造统一使用 {@link QueryWrapper}，列名会被自动校验，杜绝 SQL 注入。</li>
 * </ul>
 *
 * @param <T> 实体类型
 */
public interface BaseDao<T> {

    /**
     * 插入一条记录，返回受影响行数。自增主键会回填到实体的 id 字段。
     */
    int insert(T entity);

    /**
     * 批量插入。
     */
    int insertBatch(List<T> entities);

    /**
     * 按主键更新，返回受影响行数。
     */
    int updateById(T entity);

    /**
     * 按主键删除，返回受影响行数。
     */
    int deleteById(Object id);

    /**
     * 按条件删除，返回受影响行数。
     */
    int deleteBy(QueryWrapper<T> wrapper);

    /**
     * 按主键查询，找不到返回 null。
     */
    T selectById(Object id);

    /**
     * 按条件查询单条，找到多条时取第一条。
     */
    T selectOne(QueryWrapper<T> wrapper);

    /**
     * 按条件查询列表。
     */
    List<T> selectList(QueryWrapper<T> wrapper);

    /**
     * 按条件统计条数。
     */
    long count(QueryWrapper<T> wrapper);

    /**
     * 按条件分页查询。
     */
    PageResult<T> selectPage(QueryWrapper<T> wrapper, int page, int size);

    /**
     * 执行原生 SQL（仅用于复杂统计，参数必须用 ? 占位）。
     */
    List<T> selectBySql(String sql, Class<T> entityClass, Object... args);
}
