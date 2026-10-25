package com.cardgame.entity;

import com.cardgame.dao.Column;
import com.cardgame.dao.Table;

import java.time.LocalDateTime;

/**
 * 对局记录实体，对应 t_battle 表。
 */
@Table("t_battle")
public class Battle {

    @Column(id = true)
    private Long id;

    @Column
    private Long roomId;

    /** 模式：rank / casual */
    @Column
    private String mode;

    /** 状态：playing 进行中 / finished 已结束 */
    @Column
    private String status;

    /** 获胜者用户 id */
    @Column
    private Long winnerId;

    /** 获胜者昵称 */
    @Column
    private String winnerName;

    /** 对战回合数 */
    @Column
    private Integer rounds;

    /** 参与玩家数 */
    @Column
    private Integer playerCount;

    /** 结算奖励金币 */
    @Column
    private Integer reward;

    @Column
    private LocalDateTime startTime;

    @Column
    private LocalDateTime endTime;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getRoomId() {
        return roomId;
    }

    public void setRoomId(Long roomId) {
        this.roomId = roomId;
    }

    public String getMode() {
        return mode;
    }

    public void setMode(String mode) {
        this.mode = mode;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Long getWinnerId() {
        return winnerId;
    }

    public void setWinnerId(Long winnerId) {
        this.winnerId = winnerId;
    }

    public String getWinnerName() {
        return winnerName;
    }

    public void setWinnerName(String winnerName) {
        this.winnerName = winnerName;
    }

    public Integer getRounds() {
        return rounds;
    }

    public void setRounds(Integer rounds) {
        this.rounds = rounds;
    }

    public Integer getPlayerCount() {
        return playerCount;
    }

    public void setPlayerCount(Integer playerCount) {
        this.playerCount = playerCount;
    }

    public Integer getReward() {
        return reward;
    }

    public void setReward(Integer reward) {
        this.reward = reward;
    }

    public LocalDateTime getStartTime() {
        return startTime;
    }

    public void setStartTime(LocalDateTime startTime) {
        this.startTime = startTime;
    }

    public LocalDateTime getEndTime() {
        return endTime;
    }

    public void setEndTime(LocalDateTime endTime) {
        this.endTime = endTime;
    }
}
