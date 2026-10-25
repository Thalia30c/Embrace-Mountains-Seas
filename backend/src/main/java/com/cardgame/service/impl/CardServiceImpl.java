package com.cardgame.service.impl;

import com.cardgame.common.PageResult;
import com.cardgame.dao.CardDao;
import com.cardgame.dao.QueryWrapper;
import com.cardgame.dao.UserCardDao;
import com.cardgame.dto.UserCardVO;
import com.cardgame.entity.Card;
import com.cardgame.entity.UserCard;
import com.cardgame.service.CardService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

@Service
public class CardServiceImpl implements CardService {

    @Autowired
    private CardDao cardDao;

    @Autowired
    private UserCardDao userCardDao;

    @Override
    public PageResult<Card> pageCards(String type, String rarity, int page, int size) {
        QueryWrapper<Card> wrapper = QueryWrapper.create();
        if (type != null && !type.isBlank()) {
            wrapper.eq("type", type);
        }
        if (rarity != null && !rarity.isBlank()) {
            wrapper.eq("rarity", rarity);
        }
        wrapper.orderByAsc("id");
        return cardDao.selectPage(wrapper, page, size);
    }

    @Override
    public List<Card> listAll() {
        return cardDao.selectList(QueryWrapper.<Card>create().orderByAsc("id"));
    }

    @Override
    public Card getById(Long id) {
        Card card = cardDao.selectById(id);
        if (card == null) {
            throw new com.cardgame.common.BizException(
                    com.cardgame.common.ResultCode.NOT_FOUND, "卡牌不存在");
        }
        return card;
    }

    @Override
    public List<UserCardVO> listUserCards(Long userId) {
        List<UserCard> userCards = userCardDao.findByUserId(userId);
        if (userCards.isEmpty()) {
            return Collections.emptyList();
        }
        List<Long> cardIds = userCards.stream().map(UserCard::getCardId).toList();
        List<Card> cards = cardDao.findByIds(cardIds);
        List<UserCardVO> result = new ArrayList<>();
        for (Card card : cards) {
            int count = userCards.stream()
                    .filter(uc -> uc.getCardId().equals(card.getId()))
                    .map(uc -> uc.getCount() == null ? 0 : uc.getCount())
                    .findFirst()
                    .orElse(0);
            result.add(new UserCardVO(card, count));
        }
        return result;
    }

    @Override
    public List<Card> drawRandom(int n) {
        List<Card> all = listAll();
        if (all.isEmpty() || n <= 0) {
            return Collections.emptyList();
        }
        List<Card> pool = new ArrayList<>();
        for (Card card : all) {
            int weight = card.getWeight() == null ? 1 : Math.max(1, card.getWeight());
            for (int i = 0; i < weight; i++) {
                pool.add(card);
            }
        }
        List<Card> result = new ArrayList<>();
        for (int i = 0; i < n && !pool.isEmpty(); i++) {
            result.add(pool.remove(ThreadLocalRandom.current().nextInt(pool.size())));
        }
        return result;
    }
}
