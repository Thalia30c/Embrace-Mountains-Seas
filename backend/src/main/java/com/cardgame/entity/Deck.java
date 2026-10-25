package com.cardgame.entity;

import com.cardgame.dao.Column;
import com.cardgame.dao.Table;

import java.time.LocalDateTime;

/**
 * 卡组实体，对应 t_deck 表。
 */
@Table("t_deck")
public class Deck {

    @Column(id = true)
    private Long id;

    @Column
    private Long userId;

    /** 牌组名称 */
    @Column
    private String name;

    /** 是否默认牌组：0 否 1 是 */
    @Column
    private Integer isDefault;

    @Column
    private LocalDateTime createTime;

    @Column
    private LocalDateTime updateTime;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getIsDefault() {
        return isDefault;
    }

    public void setIsDefault(Integer isDefault) {
        this.isDefault = isDefault;
    }

    public LocalDateTime getCreateTime() {
        return createTime;
    }

    public void setCreateTime(LocalDateTime createTime) {
        this.createTime = createTime;
    }

    public LocalDateTime getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(LocalDateTime updateTime) {
        this.updateTime = updateTime;
    }
}
