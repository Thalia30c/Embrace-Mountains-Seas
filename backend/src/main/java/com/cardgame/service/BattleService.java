package com.cardgame.service;

import com.cardgame.dto.PlayCardDTO;
import com.cardgame.entity.Battle;
import com.cardgame.entity.BattleDetail;

import java.util.List;
import java.util.Map;

/**
 * 对战服务。
 */
public interface BattleService {

    /** 开局：发牌、初始化血量与回合 */
    Long startBattle(Long roomId, java.util.Set<Long> playerIds);

    /** 对局详情（含双方状态） */
    Map<String, Object> getBattleState(Long battleId, Long userId);

    /** 出牌 */
    void playCard(Long userId, PlayCardDTO dto);

    /** 抽牌 */
    Map<String, Object> drawCard(Long userId, Long battleId);

    /** 过牌 */
    void pass(Long userId, Long battleId);

    /** 认输结束对局 */
    void surrender(Long userId, Long battleId);

    /** 对局回放明细 */
    List<BattleDetail> getDetails(Long battleId);
}
