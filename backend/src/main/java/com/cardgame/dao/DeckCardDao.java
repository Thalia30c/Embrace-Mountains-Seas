package com.cardgame.dao;

import com.cardgame.entity.DeckCard;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import java.util.List;

/**
 * 牌组卡牌关联数据访问接口。
 */
@Repository
public interface DeckCardDao extends BaseDao<DeckCard> {

    /** 查询牌组内所有卡牌 */
    List<DeckCard> findByDeckId(Long deckId);

    /** 查询牌组内的卡牌 id 列表 */
    List<Long> findCardIdsByDeckId(Long deckId);

    /** 查询牌组内卡牌总数（含重复张数） */
    int countCardsByDeckId(Long deckId);
}
