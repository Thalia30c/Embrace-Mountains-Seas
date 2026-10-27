package com.cardgame.dao;

import com.cardgame.entity.Warehouse;

import java.util.List;

/**
 * warehouse 表数据访问接口：继承 {@link BaseDao} 已有的通用 CRUD，这里处理 card_list 字符串列的读写。
 */
public interface WarehouseDao extends BaseDao<Warehouse> {

    /** 读取仓库中的卡牌编号列表，card_list 为空时返回空列表 */
    List<Integer> findCardIds(Integer warehouseId);

    /** 覆盖写入卡牌编号列表 */
    int updateCardList(Integer warehouseId, String cardList);

    /** 往仓库追加一张卡牌（追加到字符串末尾） */
    int appendCard(Integer warehouseId, int cardId);

    /** 从仓库移除一张卡牌（存在多张时只移除一张） */
    int removeCard(Integer warehouseId, int cardId);
}
