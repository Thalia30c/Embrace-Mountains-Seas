package com.cardgame.dto;

import java.util.List;

/**
 * 锁卡请求体：前端把四组卡组（进攻 / 防御 / 法术 / 备选）的卡牌编号列表一并发回，
 * 后端分别写入 warehouse 表的 Attack / Defense / magic / backup 列。
 */
public class DeckSaveDTO {

    /** 当前登录用户 id */
    private Integer uid;

    /** 进攻卡组卡牌编号列表（对应 warehouse.Attack） */
    private List<Integer> attack;

    /** 防御卡组卡牌编号列表（对应 warehouse.Defense） */
    private List<Integer> defense;

    /** 法术卡组卡牌编号列表（对应 warehouse.magic） */
    private List<Integer> magic;

    /** 备选卡组卡牌编号列表（对应 warehouse.backup） */
    private List<Integer> backup;

    public Integer getUid() {
        return uid;
    }

    public void setUid(Integer uid) {
        this.uid = uid;
    }

    public List<Integer> getAttack() {
        return attack;
    }

    public void setAttack(List<Integer> attack) {
        this.attack = attack;
    }

    public List<Integer> getDefense() {
        return defense;
    }

    public void setDefense(List<Integer> defense) {
        this.defense = defense;
    }

    public List<Integer> getMagic() {
        return magic;
    }

    public void setMagic(List<Integer> magic) {
        this.magic = magic;
    }

    public List<Integer> getBackup() {
        return backup;
    }

    public void setBackup(List<Integer> backup) {
        this.backup = backup;
    }
}
