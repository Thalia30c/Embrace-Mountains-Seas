package com.cardgame.dao.impl;

import com.cardgame.dao.WarehouseDao;
import com.cardgame.entity.Warehouse;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * warehouse 表数据访问实现：通用能力来自 {@link BaseDaoImpl}，这里处理 card_list 逗号分隔字符串的读取。
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
}
