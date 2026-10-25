package com.cardgame.dao;

import com.cardgame.entity.DeckCard;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 牌组卡牌关联数据访问实现。
 */
@Repository
public class DeckCardDaoImpl extends BaseDaoImpl<DeckCard> implements DeckCardDao {

    public DeckCardDaoImpl(JdbcTemplate jdbcTemplate) {
        super(jdbcTemplate, DeckCard.class);
    }

    @Override
    public List<DeckCard> findByDeckId(Long deckId) {
        return selectList(QueryWrapper.<DeckCard>create().eq("deck_id", deckId));
    }

    @Override
    public List<Long> findCardIdsByDeckId(Long deckId) {
        return queryMaps("SELECT card_id FROM t_deck_card WHERE deck_id = ?", deckId)
                .stream()
                .map(m -> ((Number) m.get("card_id")).longValue())
                .toList();
    }

    @Override
    public int countCardsByDeckId(Long deckId) {
        Integer count = jdbcTemplate.queryForObject(
                "SELECT COALESCE(SUM(count), 0) FROM t_deck_card WHERE deck_id = ?", Integer.class, deckId);
        return count == null ? 0 : count;
    }
}
