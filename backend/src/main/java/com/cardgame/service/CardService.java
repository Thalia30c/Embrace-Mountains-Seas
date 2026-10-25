package com.cardgame.service;

import com.cardgame.common.PageResult;
import com.cardgame.dto.UserCardVO;
import com.cardgame.entity.Card;

import java.util.List;

/**
 * 卡牌服务：卡牌图鉴与用户收藏。
 */
public interface CardService {

    /** 卡牌图鉴分页查询 */
    PageResult<Card> pageCards(String type, String rarity, int page, int size);

    /** 全部卡牌（对局初始化用） */
    List<Card> listAll();

    /** 卡牌详情 */
    Card getById(Long id);

    /** 用户拥有的卡牌（含持有数量） */
    List<UserCardVO> listUserCards(Long userId);

    /** 随机抽 n 张卡，按权重 */
    List<Card> drawRandom(int n);
}
