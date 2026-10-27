package com.cardgame.entity;

import com.cardgame.dao.Column;
import com.cardgame.dao.Table;

/**
 * 仓库信息表 warehouse 对应的实体。
 *
 * <p>字段：warehouse_id / card_list（卡牌编号以逗号分隔，如 1,1,2,3）</p>
 */
@Table("warehouse")
public class Warehouse {

    /** 仓库编号，自增 */
    @Column(value = "warehouse_id", id = true)
    private Integer warehouseId;

    /** 卡牌编号列表字符串，逗号分隔 */
    @Column("card_list")
    private String cardList;

    public Integer getWarehouseId() { return warehouseId; }
    public void setWarehouseId(Integer warehouseId) { this.warehouseId = warehouseId; }
    public String getCardList() { return cardList; }
    public void setCardList(String cardList) { this.cardList = cardList; }

    @Override
    public String toString() {
        return "Warehouse{warehouseId=" + warehouseId + ", cardList='" + cardList + "'}";
    }
}
