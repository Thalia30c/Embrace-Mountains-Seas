package com.cardgame.dao.impl;

import com.cardgame.dao.WarehouseDao;
import com.cardgame.entity.Warehouse;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * warehouse 表数据访问实现：通用能力来自 {@link BaseDaoImpl}，这里处理 card_list 逗号分隔字符串的读写。
 */
@Repository
public class WarehouseDaoImpl extends BaseDaoImpl<Warehouse> implements WarehouseDao {

    private static final String SEPARATOR = ",";

    public WarehouseDaoImpl(JdbcTemplate jdbcTemplate) {
        super(jdbcTemplate, Warehouse.class);
    }

    @Override
    public List<Integer> findCardIds(Integer warehouseId) {
        Warehouse warehouse = selectById(warehouseId);
        if (warehouse == null || warehouse.getCardList() == null || warehouse.getCardList().isEmpty()) {
            return Collections.emptyList();
        }
        List<Integer> ids = new ArrayList<>();
        for (String part : warehouse.getCardList().split(SEPARATOR)) {
            String trimmed = part.trim();
            if (!trimmed.isEmpty()) {
                ids.add(Integer.valueOf(trimmed));
            }
        }
        return ids;
    }

    @Override
    public int updateCardList(Integer warehouseId, String cardList) {
        return execute("UPDATE `warehouse` SET `card_list` = ? WHERE `warehouse_id` = ?", cardList, warehouseId);
    }

    @Override
    public int appendCard(Integer warehouseId, int cardId) {
        Warehouse warehouse = selectById(warehouseId);
        if (warehouse == null) {
            return 0;
        }
        String oldList = warehouse.getCardList();
        String newList = (oldList == null || oldList.isEmpty()) ? String.valueOf(cardId) : oldList + SEPARATOR + cardId;
        return updateCardList(warehouseId, newList);
    }

    @Override
    public int removeCard(Integer warehouseId, int cardId) {
        List<Integer> ids = findCardIds(warehouseId);
        if (!ids.remove(Integer.valueOf(cardId))) {
            return 0;
        }
        StringBuilder sb = new StringBuilder();
        for (Integer id : ids) {
            sb.append(sb.length() > 0 ? SEPARATOR : "").append(id);
        }
        return updateCardList(warehouseId, sb.toString());
    }
}
