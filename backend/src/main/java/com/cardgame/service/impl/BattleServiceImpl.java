package com.cardgame.service.impl;

import com.cardgame.common.BizException;
import com.cardgame.common.ResultCode;
import com.cardgame.dao.BattleDao;
import com.cardgame.dao.BattleDetailDao;
import com.cardgame.dao.CardDao;
import com.cardgame.dao.QueryWrapper;
import com.cardgame.dao.UserDao;
import com.cardgame.dto.PlayCardDTO;
import com.cardgame.entity.Battle;
import com.cardgame.entity.BattleDetail;
import com.cardgame.entity.Card;
import com.cardgame.entity.User;
import com.cardgame.service.BattleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 对战服务实现。
 *
 * <p>对局中的实时状态（手牌、血量、当前回合）放在内存 Map 中，
 * 对局结束只把结果落库。演示阶段够用，正式部署建议换成 Redis 或独立状态表。</p>
 */
@Service
public class BattleServiceImpl implements BattleService {

    /** 每人初始手牌数 */
    private static final int INITIAL_HAND_SIZE = 5;

    /** 玩家初始血量 */
    private static final int INITIAL_HP = 30;

    /** 每次出牌获得的费用上限 */
    private static final int MAX_COST_PER_TURN = 5;

    /** 一局最多多少回合，超过判平局 */
    private static final int MAX_ROUNDS = 30;

    /** 对局中的实时状态 */
    static class BattleState {
        Long battleId;
        Long roomId;
        Set<Long> playerIds;
        Map<Long, List<Card>> hands = new HashMap<>();
        Map<Long, Integer> hp = new HashMap<>();
        Map<Long, Integer> cost = new HashMap<>();
        Map<Long, String> names = new HashMap<>();
        Long currentTurnUserId;
        int round = 1;
        boolean finished = false;
    }

    private final Map<Long, BattleState> states = new ConcurrentHashMap<>();

    @Autowired
    private BattleDao battleDao;

    @Autowired
    private BattleDetailDao battleDetailDao;

    @Autowired
    private CardDao cardDao;

    @Autowired
    private UserDao userDao;

    @Override
    public Long startBattle(Long roomId, Set<Long> playerIds) {
        Battle battle = new Battle();
        battle.setRoomId(roomId);
        battle.setMode("casual");
        battle.setStatus("playing");
        battle.setRounds(0);
        battle.setPlayerCount(playerIds.size());
        battle.setReward(0);
        battle.setStartTime(LocalDateTime.now());
        battleDao.insert(battle);

        BattleState state = new BattleState();
        state.battleId = battle.getId();
        state.roomId = roomId;
        state.playerIds = playerIds;
        state.hands = new LinkedHashMap<>();
        state.hp = new HashMap<>();
        state.cost = new HashMap<>();
        state.names = new HashMap<>();

        for (Long userId : playerIds) {
            User user = userDao.selectById(userId);
            state.names.put(userId, user == null ? "玩家" + userId : user.getNickname());
            state.hp.put(userId, INITIAL_HP);
            state.cost.put(userId, 0);
            // 发牌：随机取 INITIAL_HAND_SIZE 张
            List<Card> hand = new ArrayList<>();
            List<Card> pool = cardDao.selectList(QueryWrapper.<Card>create().orderByAsc("id"));
            Collections.shuffle(pool);
            for (int i = 0; i < INITIAL_HAND_SIZE && i < pool.size(); i++) {
                hand.add(pool.get(i));
            }
            state.hands.put(userId, hand);
        }
        state.currentTurnUserId = playerIds.iterator().next();
        states.put(battle.getId(), state);

        // 记录开局明细
        for (Long userId : playerIds) {
            saveDetail(battle.getId(), userId, state.names.get(userId), 0, "draw", null, null, 0);
        }
        return battle.getId();
    }

    @Override
    public Map<String, Object> getBattleState(Long battleId, Long userId) {
        BattleState state = requireState(battleId);
        Battle battle = battleDao.selectById(battleId);

        Map<String, Object> result = new HashMap<>();
        result.put("battleId", battleId);
        result.put("status", state.finished ? "finished" : "playing");
        result.put("round", state.round);
        result.put("currentTurnUserId", state.currentTurnUserId);
        result.put("maxCost", MAX_COST_PER_TURN);

        // 玩家列表
        List<Map<String, Object>> players = new ArrayList<>();
        for (Long pid : state.playerIds) {
            Map<String, Object> p = new HashMap<>();
            p.put("userId", pid);
            p.put("nickname", state.names.get(pid));
            p.put("hp", state.hp.get(pid));
            p.put("cost", state.cost.get(pid));
            // 只返回自己的手牌，隐藏对手手牌
            if (pid.equals(userId)) {
                p.put("hand", state.hands.get(pid));
            } else {
                p.put("handCount", state.hands.get(pid).size());
            }
            players.add(p);
        }
        result.put("players", players);

        // 我的手牌单独放一份，前端渲染方便
        result.put("myHand", state.hands.getOrDefault(userId, List.of()));
        result.put("winnerId", battle == null ? null : battle.getWinnerId());
        result.put("winnerName", battle == null ? null : battle.getWinnerName());
        return result;
    }

    @Override
    public void playCard(Long userId, PlayCardDTO dto) {
        BattleState state = requireState(dto.getBattleId());
        if (state.finished) {
            throw new BizException(ResultCode.BATTLE_FINISHED);
        }
        checkTurn(state, userId);

        List<Card> hand = state.hands.get(userId);
        Card card = hand.stream()
                .filter(c -> c.getId().equals(dto.getCardId()))
                .findFirst()
                .orElseThrow(() -> new BizException(ResultCode.CARD_NOT_ENOUGH, "手牌中没有这张卡"));

        int cost = state.cost.get(userId);
        int cardCost = card.getCost() == null ? 1 : card.getCost();
        if (cardCost > cost) {
            throw new BizException(ResultCode.CONFLICT, "本回合费用不足（剩 " + cost + "）");
        }
        state.cost.put(userId, cost - cardCost);

        // 结算卡牌效果
        int damage = 0;
        if ("attack".equals(card.getType())) {
            Long targetId = dto.getTargetUserId() != null
                    ? dto.getTargetUserId()
                    : findOpponent(state, userId);
            damage = card.getAttack() == null ? 0 : card.getAttack();
            if (targetId != null && state.hp.containsKey(targetId)) {
                int defense = 0;
                // 防御值取目标当前保留的护甲（这里简化为 0，实际可加护甲状态）
                int newHp = state.hp.get(targetId) - Math.max(0, damage - defense);
                state.hp.put(targetId, Math.max(0, newHp));
            }
        } else if ("defense".equals(card.getType())) {
            int add = card.getDefense() == null ? 0 : card.getDefense();
            state.hp.put(userId, Math.min(INITIAL_HP, state.hp.get(userId) + add / 2));
        } else if ("skill".equals(card.getType())) {
            int heal = card.getCost() != null && card.getCost() >= 5 ? 10 : 5;
            state.hp.put(userId, Math.min(INITIAL_HP, state.hp.get(userId) + heal));
        }

        hand.remove(card);
        saveDetail(state.battleId, userId, state.names.get(userId), state.round,
                "play", card.getId(), card.getName(), damage);

        // 判定胜负
        if (checkFinish(state)) {
            return;
        }
        nextTurn(state);
    }

    @Override
    public Map<String, Object> drawCard(Long userId, Long battleId) {
        BattleState state = requireState(battleId);
        if (state.finished) {
            throw new BizException(ResultCode.BATTLE_FINISHED);
        }
        checkTurn(state, userId);

        List<Card> pool = cardDao.selectList(QueryWrapper.<Card>create().orderByAsc("id"));
        Collections.shuffle(pool);
        Card card = pool.isEmpty() ? null : pool.get(0);
        if (card != null) {
            state.hands.get(userId).add(card);
            state.cost.put(userId, Math.min(MAX_COST_PER_TURN, state.cost.get(userId) + 1));
            saveDetail(state.battleId, userId, state.names.get(userId), state.round,
                    "draw", card.getId(), card.getName(), 0);
        }
        Map<String, Object> result = new HashMap<>();
        result.put("card", card);
        result.put("hand", state.hands.get(userId));
        result.put("cost", state.cost.get(userId));
        return result;
    }

    @Override
    public void pass(Long userId, Long battleId) {
        BattleState state = requireState(battleId);
        if (state.finished) {
            throw new BizException(ResultCode.BATTLE_FINISHED);
        }
        checkTurn(state, userId);
        saveDetail(state.battleId, userId, state.names.get(userId), state.round, "pass", null, null, 0);
        nextTurn(state);
    }

    @Override
    public void surrender(Long userId, Long battleId) {
        BattleState state = requireState(battleId);
        if (state.finished) {
            throw new BizException(ResultCode.BATTLE_FINISHED);
        }
        state.hp.put(userId, 0);
        checkFinish(state);
    }

    @Override
    public List<BattleDetail> getDetails(Long battleId) {
        return battleDetailDao.findByBattleId(battleId);
    }

    // ------------------------------------------------------------------ 内部方法

    /** 校验是否轮到该玩家出牌 */
    private void checkTurn(BattleState state, Long userId) {
        if (!state.currentTurnUserId.equals(userId)) {
            throw new BizException(ResultCode.NOT_YOUR_TURN);
        }
    }

    /** 推进到下一个回合 */
    private void nextTurn(BattleState state) {
        List<Long> order = new ArrayList<>(state.playerIds);
        int idx = order.indexOf(state.currentTurnUserId);
        idx = (idx + 1) % order.size();
        state.currentTurnUserId = order.get(idx);
        // 回合数 +1，新回合重置费用
        state.round++;
        for (Long pid : order) {
            state.cost.put(pid, MAX_COST_PER_TURN);
        }
        if (state.round > MAX_ROUNDS) {
            finishBattle(state, null);
        }
    }

    /** 检查是否有玩家血量归零 */
    private boolean checkFinish(BattleState state) {
        for (Long pid : state.playerIds) {
            if (state.hp.get(pid) <= 0) {
                finishBattle(state, pid);
                return true;
            }
        }
        return false;
    }

    /** 结束对局并落库结算 */
    private void finishBattle(BattleState state, Long winnerId) {
        state.finished = true;
        Battle battle = battleDao.selectById(state.battleId);
        if (battle == null) {
            return;
        }
        battle.setStatus("finished");
        battle.setWinnerId(winnerId);
        battle.setWinnerName(winnerId == null ? "平局" : state.names.get(winnerId));
        battle.setRounds(state.round);
        battle.setEndTime(LocalDateTime.now());
        int reward = winnerId == null ? 10 : 30;
        battle.setReward(reward);
        battleDao.updateById(battle);

        // 更新玩家胜负场次与金币
        for (Long pid : state.playerIds) {
            User user = userDao.selectById(pid);
            if (user == null) {
                continue;
            }
            boolean win = winnerId != null && winnerId.equals(pid);
            user.setWinCount((user.getWinCount() == null ? 0 : user.getWinCount()) + (win ? 1 : 0));
            user.setLoseCount((user.getLoseCount() == null ? 0 : user.getLoseCount()) + (win ? 0 : 1));
            user.setCoins((user.getCoins() == null ? 0 : user.getCoins()) + (win ? reward : reward / 3));
            user.setUpdateTime(LocalDateTime.now());
            userDao.updateById(user);
        }
    }

    /** 找一个对手作为攻击目标 */
    private Long findOpponent(BattleState state, Long userId) {
        for (Long pid : state.playerIds) {
            if (!pid.equals(userId)) {
                return pid;
            }
        }
        return null;
    }

    /** 取对局状态，不存在直接报错 */
    private BattleState requireState(Long battleId) {
        BattleState state = states.get(battleId);
        if (state == null) {
            throw new BizException(ResultCode.NOT_FOUND, "对局不存在或已过期");
        }
        return state;
    }

    /** 落一条对局明细 */
    private void saveDetail(Long battleId, Long userId, String userName, int round,
                            String action, Long cardId, String cardName, int score) {
        BattleDetail detail = new BattleDetail();
        detail.setBattleId(battleId);
        detail.setUserId(userId);
        detail.setUserName(userName);
        detail.setRound(round);
        detail.setAction(action);
        detail.setCardId(cardId);
        detail.setCardName(cardName);
        detail.setScore(score);
        detail.setCreateTime(LocalDateTime.now());
        battleDetailDao.insert(detail);
    }
}
