package com.cardgame.service.impl;

import com.cardgame.common.BizException;
import com.cardgame.common.ResultCode;
import com.cardgame.dao.CardDao;
import com.cardgame.dao.DeckCardDao;
import com.cardgame.dao.DeckDao;
import com.cardgame.dao.QueryWrapper;
import com.cardgame.entity.Card;
import com.cardgame.entity.Deck;
import com.cardgame.entity.DeckCard;
import com.cardgame.service.DeckService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class DeckServiceImpl implements DeckService {

    /** 单个牌组最多放入的卡牌总张数 */
    private static final int MAX_DECK_SIZE = 30;

    @Autowired
    private DeckDao deckDao;

    @Autowired
    private DeckCardDao deckCardDao;

    @Autowired
    private CardDao cardDao;

    @Override
    public List<Map<String, Object>> listMyDecks(Long userId) {
        List<Deck> decks = deckDao.findByUserId(userId);
        List<Map<String, Object>> result = new ArrayList<>();
        for (Deck deck : decks) {
            Map<String, Object> item = new HashMap<>();
            item.put("deck", deck);
            item.put("cards", loadDeckCards(deck.getId()));
            item.put("totalCount", deckCardDao.countCardsByDeckId(deck.getId()));
            result.add(item);
        }
        return result;
    }

    @Override
    @Transactional
    public Deck createDeck(Long userId, String name) {
        Deck deck = new Deck();
        deck.setUserId(userId);
        deck.setName(name == null || name.isBlank() ? "新牌组" : name);
        // 第一个牌组自动设为默认
        boolean first = deckDao.findByUserId(userId).isEmpty();
        deck.setIsDefault(first ? 1 : 0);
        deck.setCreateTime(LocalDateTime.now());
        deck.setUpdateTime(LocalDateTime.now());
        deckDao.insert(deck);
        return deck;
    }

    @Override
    @Transactional
    public void deleteDeck(Long userId, Long deckId) {
        Deck deck = checkOwnership(userId, deckId);
        deckCardDao.deleteBy(QueryWrapper.<DeckCard>create().eq("deck_id", deck.getId()));
        deckDao.deleteById(deck.getId());
    }

    @Override
    @Transactional
    public Deck setDefault(Long userId, Long deckId) {
        Deck deck = checkOwnership(userId, deckId);
        // 先把该用户其它牌组的默认标记清掉
        for (Deck d : deckDao.findByUserId(userId)) {
            if (d.getId().equals(deckId)) {
                continue;
            }
            if (d.getIsDefault() != null && d.getIsDefault() == 1) {
                d.setIsDefault(0);
                d.setUpdateTime(LocalDateTime.now());
                deckDao.updateById(d);
            }
        }
        deck.setIsDefault(1);
        deck.setUpdateTime(LocalDateTime.now());
        deckDao.updateById(deck);
        return deck;
    }

    @Override
    @Transactional
    public void addCard(Long userId, Long deckId, Long cardId) {
        Deck deck = checkOwnership(userId, deckId);
        if (cardDao.selectById(cardId) == null) {
            throw new BizException(ResultCode.NOT_FOUND, "卡牌不存在");
        }
        if (deckCardDao.countCardsByDeckId(deck.getId()) >= MAX_DECK_SIZE) {
            throw new BizException(ResultCode.CONFLICT, "牌组已满，最多 " + MAX_DECK_SIZE + " 张");
        }
        DeckCard exist = deckCardDao.selectOne(QueryWrapper.<DeckCard>create()
                .eq("deck_id", deck.getId())
                .eq("card_id", cardId));
        if (exist != null) {
            throw new BizException(ResultCode.CARD_ALREADY_IN_DECK);
        }
        DeckCard dc = new DeckCard();
        dc.setDeckId(deck.getId());
        dc.setCardId(cardId);
        dc.setCount(1);
        deckCardDao.insert(dc);

        deck.setUpdateTime(LocalDateTime.now());
        deckDao.updateById(deck);
    }

    @Override
    @Transactional
    public void removeCard(Long userId, Long deckId, Long cardId) {
        Deck deck = checkOwnership(userId, deckId);
        int rows = deckCardDao.deleteBy(QueryWrapper.<DeckCard>create()
                .eq("deck_id", deck.getId())
                .eq("card_id", cardId));
        if (rows == 0) {
            throw new BizException(ResultCode.NOT_FOUND, "牌组中没有这张卡");
        }
        deck.setUpdateTime(LocalDateTime.now());
        deckDao.updateById(deck);
    }

    @Override
    public Deck getBattleDeck(Long userId, Long deckId) {
        if (deckId != null) {
            return checkOwnership(userId, deckId);
        }
        Deck defaultDeck = deckDao.findDefaultByUserId(userId);
        if (defaultDeck != null) {
            return defaultDeck;
        }
        List<Deck> decks = deckDao.findByUserId(userId);
        if (decks.isEmpty()) {
            throw new BizException(ResultCode.DECK_EMPTY, "请先创建并配置牌组");
        }
        return decks.get(0);
    }

    /** 取出牌组内的卡牌明细（含卡牌完整信息） */
    private List<Map<String, Object>> loadDeckCards(Long deckId) {
        List<DeckCard> deckCards = deckCardDao.findByDeckId(deckId);
        List<Map<String, Object>> list = new ArrayList<>();
        for (DeckCard dc : deckCards) {
            Card card = cardDao.selectById(dc.getCardId());
            if (card == null) {
                continue;
            }
            Map<String, Object> item = new HashMap<>();
            item.put("card", card);
            item.put("count", dc.getCount());
            list.add(item);
        }
        return list;
    }

    /** 校验牌组归属，非本人牌组直接拒绝 */
    private Deck checkOwnership(Long userId, Long deckId) {
        Deck deck = deckDao.selectById(deckId);
        if (deck == null) {
            throw new BizException(ResultCode.NOT_FOUND, "牌组不存在");
        }
        if (!deck.getUserId().equals(userId)) {
            throw new BizException(ResultCode.FORBIDDEN, "只能操作自己的牌组");
        }
        return deck;
    }
}
