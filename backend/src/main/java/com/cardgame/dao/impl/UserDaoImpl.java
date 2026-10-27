package com.cardgame.dao.impl;

import com.cardgame.dao.UserDao;
import com.cardgame.entity.User;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * user 表数据访问实现：通用能力来自 {@link BaseDaoImpl}，这里实现用户相关的业务查询。
 */
@Repository
public class UserDaoImpl extends BaseDaoImpl<User> implements UserDao {

    public UserDaoImpl(JdbcTemplate jdbcTemplate) {
        super(jdbcTemplate, User.class);
    }

    @Override
    public User findByAccount(String userAccount) {
        return selectOne("user_account", userAccount);
    }

    @Override
    public List<User> findByGameLevel(Integer gameLevel) {
        return selectList("game_level", gameLevel, "score DESC", 0);
    }

    @Override
    public List<User> findTopByScore(int limit) {
        return selectTop("score DESC", limit);
    }
}
