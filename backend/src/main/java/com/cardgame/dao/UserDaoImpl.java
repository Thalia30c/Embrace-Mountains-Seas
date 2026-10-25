package com.cardgame.dao;

import com.cardgame.entity.User;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 用户数据访问实现。所有 SQL 都收敛在这一层，Service 不直接接触 JdbcTemplate。
 */
@Repository
public class UserDaoImpl extends BaseDaoImpl<User> implements UserDao {

    public UserDaoImpl(JdbcTemplate jdbcTemplate) {
        super(jdbcTemplate, User.class);
    }

    @Override
    public User findByUsername(String username) {
        return selectOne(QueryWrapper.<User>create().eq("username", username));
    }

    @Override
    public User findByOpenid(String openid) {
        return selectOne(QueryWrapper.<User>create().eq("openid", openid));
    }

    @Override
    public List<User> findTopByWinCount(int limit) {
        return selectList(QueryWrapper.<User>create()
                .orderByDesc("win_count")
                .orderByDesc("coins")
                .limit(limit));
    }
}
