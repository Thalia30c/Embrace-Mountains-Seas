package com.cardgame.service;

import com.cardgame.entity.Deck;

import java.util.List;
import java.util.Map;

/**
 * 牌组服务。
 */
public interface DeckService {

    /** 我的牌组列表（含卡牌明细） */
    List<Map<String, Object>> listMyDecks(Long userId);

    /** 创建牌组 */
    Deck createDeck(Long userId, String name);

    /** 删除牌组 */
    void deleteDeck(Long userId, Long deckId);

    /** 设置默认牌组 */
    Deck setDefault(Long userId, Long deckId);

    /** 往牌组加卡 */
    void addCard(Long userId, Long deckId, Long cardId);

    /** 从牌组移除卡 */
    void removeCard(Long userId, Long deckId, Long cardId);

    /** 取用户出牌用的牌组（优先默认，其次第一个） */
    Deck getBattleDeck(Long userId, Long deckId);
}
