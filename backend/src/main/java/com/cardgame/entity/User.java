package com.cardgame.entity;

import com.cardgame.dao.Column;
import com.cardgame.dao.Table;

import java.time.LocalDateTime;

/**
 * 用户实体，对应 t_user 表。
 */
@Table("t_user")
public class User {

    @Column(id = true)
    private Long id;

    /** 微信小程序 openid，唯一 */
    @Column
    private String openid;

    /** 登录用户名，唯一 */
    @Column
    private String username;

    /** 登录密码（示例工程存明文，生产请用 BCrypt） */
    @Column
    private String password;

    /** 昵称 */
    @Column
    private String nickname;

    /** 头像地址 */
    @Column
    private String avatar;

    /** 金币数 */
    @Column
    private Integer coins;

    /** 胜场数 */
    @Column
    private Integer winCount;

    /** 负场数 */
    @Column
    private Integer loseCount;

    /** 段位 */
    @Column
    private String rank;

    /** 是否封禁：0 正常 1 封禁 */
    @Column
    private Integer banned;

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

    public String getOpenid() {
        return openid;
    }

    public void setOpenid(String openid) {
        this.openid = openid;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public String getAvatar() {
        return avatar;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }

    public Integer getCoins() {
        return coins;
    }

    public void setCoins(Integer coins) {
        this.coins = coins;
    }

    public Integer getWinCount() {
        return winCount;
    }

    public void setWinCount(Integer winCount) {
        this.winCount = winCount;
    }

    public Integer getLoseCount() {
        return loseCount;
    }

    public void setLoseCount(Integer loseCount) {
        this.loseCount = loseCount;
    }

    public String getRank() {
        return rank;
    }

    public void setRank(String rank) {
        this.rank = rank;
    }

    public Integer getBanned() {
        return banned;
    }

    public void setBanned(Integer banned) {
        this.banned = banned;
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
