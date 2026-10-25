package com.cardgame.entity;

import com.cardgame.dao.Column;
import com.cardgame.dao.Table;

import java.time.LocalDateTime;

/**
 * 对局明细实体，对应 t_battle_detail 表：记录每一步出牌，用于回放与战绩统计。
 */
@Table("t_battle_detail")
public class BattleDetail {

    @Column(id = true)
    private Long id;

    @Column
    private Long battleId;

    /** 操作用户 id */
    @Column
    private Long userId;

    /** 操作人昵称 */
    @Column
    private String userName;

    /** 回合序号 */
    @Column
    private Integer round;

    /** 动作类型：play 出牌 / draw 抽牌 / skill 使用技能 / pass 过牌 */
    @Column
    private String action;

    /** 关联卡牌 id */
    @Column
    private Long cardId;

    /** 卡牌名称冗余 */
    @Column
    private String cardName;

    /** 本次得分 */
    @Column
    private Integer score;

    @Column
    private LocalDateTime createTime;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getBattleId() {
        return battleId;
    }

    public void setBattleId(Long battleId) {
        this.battleId = battleId;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public Integer getRound() {
        return round;
    }

    public void setRound(Integer round) {
        this.round = round;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public Long getCardId() {
        return cardId;
    }

    public void setCardId(Long cardId) {
        this.cardId = cardId;
    }

    public String getCardName() {
        return cardName;
    }

    public void setCardName(String cardName) {
        this.cardName = cardName;
    }

    public Integer getScore() {
        return score;
    }

    public void setScore(Integer score) {
        this.score = score;
    }

    public LocalDateTime getCreateTime() {
        return createTime;
    }

    public void setCreateTime(LocalDateTime createTime) {
        this.createTime = createTime;
    }
}
