package com.cardgame.dto;

import jakarta.validation.constraints.NotNull;

/**
 * 出牌请求。
 */
public class PlayCardDTO {

    /** 对局 id */
    @NotNull(message = "不能为空")
    private Long battleId;

    /** 出的卡牌 id */
    @NotNull(message = "不能为空")
    private Long cardId;

    /** 目标玩家 id，攻击类卡牌需要 */
    private Long targetUserId;

    public Long getBattleId() {
        return battleId;
    }

    public void setBattleId(Long battleId) {
        this.battleId = battleId;
    }

    public Long getCardId() {
        return cardId;
    }

    public void setCardId(Long cardId) {
        this.cardId = cardId;
    }

    public Long getTargetUserId() {
        return targetUserId;
    }

    public void setTargetUserId(Long targetUserId) {
        this.targetUserId = targetUserId;
    }
}
