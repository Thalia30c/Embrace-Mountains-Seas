package com.cardgame.dto;

/**
 * 登录成功返回体。
 */
public class LoginVO {

    private Long userId;
    private String token;
    private long expireSeconds;
    private String nickname;
    private String avatar;
    private Integer coins;
    private String rank;

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public long getExpireSeconds() {
        return expireSeconds;
    }

    public void setExpireSeconds(long expireSeconds) {
        this.expireSeconds = expireSeconds;
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

    public String getRank() {
        return rank;
    }

    public void setRank(String rank) {
        this.rank = rank;
    }
}
