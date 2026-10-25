package com.cardgame.entity;

import com.cardgame.dao.Column;
import com.cardgame.dao.Table;

/**
 * 卡牌实体，对应 t_card 表：卡牌图鉴（基础配置）。
 */
@Table("t_card")
public class Card {

    @Column(id = true)
    private Long id;

    /** 卡牌名称 */
    @Column
    private String name;

    /** 卡牌描述 */
    @Column
    private String description;

    /** 类型：attack 攻击 / defense 防御 / skill 技能 */
    @Column
    private String type;

    /** 稀有度：common 普通 / rare 稀有 / epic 史诗 / legend 传说 */
    @Column
    private String rarity;

    /** 攻击力 */
    @Column
    private Integer attack;

    /** 防御力 */
    @Column
    private Integer defense;

    /** 费用 */
    @Column
    private Integer cost;

    /** 卡牌图片地址 */
    @Column
    private String image;

    /** 权重，用于随机发牌时的出现概率 */
    @Column
    private Integer weight;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getRarity() {
        return rarity;
    }

    public void setRarity(String rarity) {
        this.rarity = rarity;
    }

    public Integer getAttack() {
        return attack;
    }

    public void setAttack(Integer attack) {
        this.attack = attack;
    }

    public Integer getDefense() {
        return defense;
    }

    public void setDefense(Integer defense) {
        this.defense = defense;
    }

    public Integer getCost() {
        return cost;
    }

    public void setCost(Integer cost) {
        this.cost = cost;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public Integer getWeight() {
        return weight;
    }

    public void setWeight(Integer weight) {
        this.weight = weight;
    }
}
