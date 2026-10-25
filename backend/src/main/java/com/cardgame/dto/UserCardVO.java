package com.cardgame.dto;

import com.cardgame.entity.Card;

/**
 * 用户卡牌收藏展示对象：卡牌信息 + 持有数量。
 */
public class UserCardVO {

    private Card card;
    private Integer count;

    public UserCardVO() {
    }

    public UserCardVO(Card card, Integer count) {
        this.card = card;
        this.count = count;
    }

    public Card getCard() {
        return card;
    }

    public void setCard(Card card) {
        this.card = card;
    }

    public Integer getCount() {
        return count;
    }

    public void setCount(Integer count) {
        this.count = count;
    }
}
