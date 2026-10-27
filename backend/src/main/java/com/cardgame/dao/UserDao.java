package com.cardgame.dao;

import com.cardgame.entity.User;

import java.util.List;

/**
 * user 表数据访问接口：继承 {@link BaseDao} 已有的通用 CRUD，这里只声明用户相关的业务查询。
 */
public interface UserDao extends BaseDao<User> {

    /** 按登录账号查询 */
    User findByAccount(String userAccount);

    /** 按用户名查询 */
    User findByUsername(String username);

    /** 账号是否已存在 */
    boolean existsByAccount(String userAccount);

    /** 查询某个段位下的全部用户 */
    List<User> findByGameLevel(Integer gameLevel);

    /** 积分排行榜：按积分降序取前 limit 名 */
    List<User> findTopByScore(int limit);

    /** 设置指定用户的积分为 score（覆盖原值） */
    int updateScore(Integer uid, int score);

    /** 给指定用户累加积分，delta 可为负数 */
    int addScore(Integer uid, int delta);
}
