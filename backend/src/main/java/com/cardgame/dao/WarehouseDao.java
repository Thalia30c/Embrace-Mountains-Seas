package com.cardgame.dao;

import com.cardgame.entity.Warehouse;

import java.util.List;

/**
 * warehouse 表数据访问接口：继承 {@link BaseDao} 已有的通用 CRUD，这里处理 card_list 字符串列的读写。
 */
public interface WarehouseDao extends BaseDao<Warehouse> {

    /** 读取仓库中的卡牌编号列表，card_list 为空时返回空列表 */
    List<Integer> findCardIds(Integer warehouseId);
}
