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

    /** 进攻卡组卡牌编号，逗号分隔 */
    @Column("Attack")
    private String attack;

    /** 防御卡组卡牌编号，逗号分隔 */
    @Column("Defense")
    private String defense;

    /** 法术卡组卡牌编号，逗号分隔 */
    @Column("magic")
    private String magic;

    /** 备选卡组卡牌编号，逗号分隔 */
    @Column("backup")
    private String backup;

    public Integer getWarehouseId() { return warehouseId; }
    public void setWarehouseId(Integer warehouseId) { this.warehouseId = warehouseId; }
    public String getCardList() { return cardList; }
    public void setCardList(String cardList) { this.cardList = cardList; }

    public String getAttack() { return attack; }
    public void setAttack(String attack) { this.attack = attack; }
    public String getDefense() { return defense; }
    public void setDefense(String defense) { this.defense = defense; }
    public String getMagic() { return magic; }
    public void setMagic(String magic) { this.magic = magic; }
    public String getBackup() { return backup; }
    public void setBackup(String backup) { this.backup = backup; }

    @Override
    public String toString() {
        return "Warehouse{warehouseId=" + warehouseId + ", cardList='" + cardList + "'}";
    }
}
