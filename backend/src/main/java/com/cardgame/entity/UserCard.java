package com.cardgame.entity;

import com.cardgame.dao.Column;
import com.cardgame.dao.Table;

import java.time.LocalDateTime;

/**
 * 用户卡牌收藏实体，对应 t_user_card 表：记录用户拥有哪些卡、各有几张。
 */
@Table("t_user_card")
public class UserCard {

    @Column(id = true)
    private Long id;

    @Column
    private Long userId;

    @Column
    private Long cardId;

    /** 拥有数量 */
    @Column
    private Integer count;

    @Column
    private LocalDateTime createTime;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
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

    public LocalDateTime getCreateTime() {
        return createTime;
    }

    public void setCreateTime(LocalDateTime createTime) {
        this.createTime = createTime;
    }
}
