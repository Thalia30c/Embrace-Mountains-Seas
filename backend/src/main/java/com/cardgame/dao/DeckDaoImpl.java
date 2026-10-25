package com.cardgame.dao;

import com.cardgame.entity.Deck;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 卡组数据访问实现。
 */
@Repository
public class DeckDaoImpl extends BaseDaoImpl<Deck> implements DeckDao {

    public DeckDaoImpl(JdbcTemplate jdbcTemplate) {
        super(jdbcTemplate, Deck.class);
    }

    @Override
    public List<Deck> findByUserId(Long userId) {
        return selectList(QueryWrapper.<Deck>create()
                .eq("user_id", userId)
                .orderByDesc("is_default")
                .orderByDesc("update_time"));
    }

    @Override
    public Deck findDefaultByUserId(Long userId) {
        return selectOne(QueryWrapper.<Deck>create()
                .eq("user_id", userId)
                .eq("is_default", 1));
    }
}
