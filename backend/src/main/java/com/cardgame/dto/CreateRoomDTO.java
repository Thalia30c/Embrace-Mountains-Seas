package com.cardgame.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * 创建房间请求。
 */
public class CreateRoomDTO {

    /** 房间名称 */
    private String name;

    /** 模式：rank 排位 / casual 休闲，默认 casual */
    private String mode = "casual";

    /** 人数上限，默认 2 */
    private Integer maxPlayer = 2;

    /** 房间密码，可选 */
    private String password;

    /** 使用哪个牌组出牌 */
    private Long deckId;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getMode() {
        return mode;
    }

    public void setMode(String mode) {
        this.mode = mode;
    }

    public Integer getMaxPlayer() {
        return maxPlayer;
    }

    public void setMaxPlayer(Integer maxPlayer) {
        this.maxPlayer = maxPlayer;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public Long getDeckId() {
        return deckId;
    }

    public void setDeckId(Long deckId) {
        this.deckId = deckId;
    }
}
