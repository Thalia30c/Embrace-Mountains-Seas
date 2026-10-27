package com.cardgame.dao;

import com.cardgame.entity.Card;

import java.util.List;

/**
 * card 表数据访问接口：继承 {@link BaseDao} 已有的通用 CRUD，这里只声明卡牌相关的业务查询。
 */
public interface CardDao extends BaseDao<Card> {

    /** 按品级查询：黄 / 玄 */
    List<Card> findByLevel(String cLevel);

    /** 按卡牌名字查询 */
    Card findByName(String cName);

    /** 查询全部复合型卡牌（is_compound = 1） */
    List<Card> findCompoundCards();

    /** 查询拥有指定被动技能的卡牌 */
    List<Card> findByPassiveCode(String passiveCode);

    /** 查询克制指定卡牌的卡牌（谁克制我） */
    List<Card> findCounterTo(Integer cardId);

    /**
     * 沿克制关系取克制链：A → A 克制的卡 → ...，遇到成环或 null 即停止。
     *
     * @param cardId   起始卡牌 id
     * @param maxDepth 最大深度，防止克制环导致死循环
     */
    List<Card> findCounterChain(Integer cardId, int maxDepth);
}
