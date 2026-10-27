package com.cardgame.entity;

import com.cardgame.dao.Column;
import com.cardgame.dao.Table;

/**
 * 卡牌信息汇总表 card 对应的实体，主键 id 为手工编号 1-24，非自增。
 *
 * <p>字段：id / c_name / c_level / hp / skill_name / damage_or_shield /
 * passive_code / counter_id / is_compound</p>
 */
@Table(value = "card", autoIncrement = false)
public class Card {

    /** 卡牌编号 1-24 */
    @Column(value = "id", id = true)
    private Integer id;

    /** 卡牌名字 */
    @Column("c_name")
    private String cName;

    /** 品级：黄 / 玄 */
    @Column("c_level")
    private String cLevel;

    /** 血量 */
    @Column("hp")
    private Integer hp;

    /** 主动技能名 */
    @Column("skill_name")
    private String skillName;

    /** 伤害点数或护盾值：1-6 / H1-H5 / 无 */
    @Column("damage_or_shield")
    private String damageOrShield;

    /** 被动技能编码，关联 passive_skill.code */
    @Column("passive_code")
    private String passiveCode;

    /** 克制对象卡牌 id，引用 card.id */
    @Column("counter_id")
    private Integer counterId;

    /** 是否复合型卡牌：0 否 / 1 是 */
    @Column("is_compound")
    private Integer isCompound;

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    public String getCName() { return cName; }
    public void setCName(String cName) { this.cName = cName; }
    public String getCLevel() { return cLevel; }
    public void setCLevel(String cLevel) { this.cLevel = cLevel; }
    public Integer getHp() { return hp; }
    public void setHp(Integer hp) { this.hp = hp; }
    public String getSkillName() { return skillName; }
    public void setSkillName(String skillName) { this.skillName = skillName; }
    public String getDamageOrShield() { return damageOrShield; }
    public void setDamageOrShield(String damageOrShield) { this.damageOrShield = damageOrShield; }
    public String getPassiveCode() { return passiveCode; }
    public void setPassiveCode(String passiveCode) { this.passiveCode = passiveCode; }
    public Integer getCounterId() { return counterId; }
    public void setCounterId(Integer counterId) { this.counterId = counterId; }
    public Integer getIsCompound() { return isCompound; }
    public void setIsCompound(Integer isCompound) { this.isCompound = isCompound; }

    /** 是否为复合型卡牌（同时拥有主动技能与被动技能） */
    public boolean compound() {
        return isCompound != null && isCompound == 1;
    }

    /** 是否为护盾类卡牌，damage_or_shield 以 H 开头 */
    public boolean shield() {
        return damageOrShield != null && damageOrShield.startsWith("H");
    }

    /** 是否为攻击伤害类卡牌，纯数字表示伤害点数 */
    public boolean damage() {
        return damageOrShield != null && damageOrShield.matches("\\d+");
    }

    @Override
    public String toString() {
        return "Card{id=" + id + ", cName='" + cName + "', cLevel='" + cLevel + "', hp=" + hp
                + ", damageOrShield='" + damageOrShield + "', passiveCode='" + passiveCode
                + "', counterId=" + counterId + ", isCompound=" + isCompound + '}';
    }
}
