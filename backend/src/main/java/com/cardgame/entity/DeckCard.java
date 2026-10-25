package com.cardgame.entity;

import com.cardgame.dao.Column;
import com.cardgame.dao.Table;

/**
 * 牌组卡牌关联实体，对应 t_deck_card 表：一个牌组包含哪些卡牌。
 */
@Table("t_deck_card")
public class DeckCard {

    @Column(id = true)
    private Long id;

    @Column
    private Long deckId;

    @Column
    private Long cardId;

    /** 该卡牌在牌组中的数量 */
    @Column
    private Integer count;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getDeckId() {
        return deckId;
    }

    public void setDeckId(Long deckId) {
        this.deckId = deckId;
    }

    public Long getCardId() {
        return cardId;
    }

    public void setCardId(Long cardId) {
        this.cardId = cardId;
    }

    public Integer getCount() {
        return count;
    }

    public void setCount(Integer count) {
        this.count = count;
    }
}
