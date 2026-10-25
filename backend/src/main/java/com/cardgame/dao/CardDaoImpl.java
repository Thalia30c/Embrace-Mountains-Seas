package com.cardgame.dao;

import com.cardgame.entity.Card;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.Collections;
import java.util.List;

/**
 * 卡牌图鉴数据访问实现。
 */
@Repository
public class CardDaoImpl extends BaseDaoImpl<Card> implements CardDao {

    public CardDaoImpl(JdbcTemplate jdbcTemplate) {
        super(jdbcTemplate, Card.class);
    }

    @Override
    public List<Card> findByType(String type) {
        return selectList(QueryWrapper.<Card>create().eq("type", type));
    }

    @Override
    public List<Card> findByRarity(String rarity) {
        return selectList(QueryWrapper.<Card>create().eq("rarity", rarity));
    }

    @Override
    public List<Card> findByIds(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return Collections.emptyList();
        }
        return selectList(QueryWrapper.<Card>create().in("id", ids));
    }
}
