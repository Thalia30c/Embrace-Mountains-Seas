package com.cardgame.dao.impl;

import com.cardgame.dao.CardDao;
import com.cardgame.entity.Card;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * card 表数据访问实现：通用能力来自 {@link BaseDaoImpl}，这里实现卡牌相关的业务查询。
 */
@Repository
public class CardDaoImpl extends BaseDaoImpl<Card> implements CardDao {

    public CardDaoImpl(JdbcTemplate jdbcTemplate) {
        super(jdbcTemplate, Card.class);
    }

    @Override
    public List<Card> findByLevel(String cLevel) {
        return selectList("c_level", cLevel, "id ASC", 0);
    }

    @Override
    public Card findByName(String cName) {
        return selectOne("c_name", cName);
    }

    @Override
    public List<Card> findCompoundCards() {
        return selectList("is_compound", 1, "id ASC", 0);
    }

    @Override
    public List<Card> findByPassiveCode(String passiveCode) {
        return selectList("passive_code", passiveCode, "id ASC", 0);
    }

    @Override
    public List<Card> findCounterTo(Integer cardId) {
        return selectList("counter_id", cardId, "id ASC", 0);
    }

    @Override
    public List<Card> findCounterChain(Integer cardId, int maxDepth) {
        List<Card> chain = new ArrayList<>();
        Set<Integer> visited = new HashSet<>();
        Integer current = cardId;
        while (current != null && !visited.contains(current) && chain.size() < maxDepth) {
            visited.add(current);
            Card card = selectById(current);
            if (card == null) {
                break;
            }
            chain.add(card);
            current = card.getCounterId();
        }
        return chain;
    }
}
