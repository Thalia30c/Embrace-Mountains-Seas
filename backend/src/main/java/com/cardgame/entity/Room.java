package com.cardgame.entity;

import com.cardgame.dao.Column;
import com.cardgame.dao.Table;

/**
 * 房间实体，对应 t_room 表：对战房间（大厅 -> 房间 -> 对局）。
 */
@Table("t_room")
public class Room {

    @Column(id = true)
    private Long id;

    /** 房间号，6 位数字，玩家输入房间号可快速加入 */
    @Column
    private String roomNo;

    /** 房主用户 id */
    @Column
    private Long ownerId;

    /** 房主昵称冗余字段，列表展示用 */
    @Column
    private String ownerName;

    /** 房间名称 */
    @Column
    private String name;

    /** 模式：rank 排位 / casual 休闲 */
    @Column
    private String mode;

    /** 状态：waiting 等待中 / playing 对战中 / finished 已结束 */
    @Column
    private String status;

    /** 人数上限 */
    @Column
    private Integer maxPlayer;

    /** 当前人数 */
    @Column
    private Integer currentPlayer;

    /** 密码，非空表示私密房间 */
    @Column
    private String password;

    /** 对局记录 id */
    @Column
    private Long battleId;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getRoomNo() {
        return roomNo;
    }

    public void setRoomNo(String roomNo) {
        this.roomNo = roomNo;
    }

    public Long getOwnerId() {
        return ownerId;
    }

    public void setOwnerId(Long ownerId) {
        this.ownerId = ownerId;
    }

    public String getOwnerName() {
        return ownerName;
    }

    public void setOwnerName(String ownerName) {
        this.ownerName = ownerName;
    }

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

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Integer getMaxPlayer() {
        return maxPlayer;
    }

    public void setMaxPlayer(Integer maxPlayer) {
        this.maxPlayer = maxPlayer;
    }

    public Integer getCurrentPlayer() {
        return currentPlayer;
    }

    public void setCurrentPlayer(Integer currentPlayer) {
        this.currentPlayer = currentPlayer;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public Long getBattleId() {
        return battleId;
    }

    public void setBattleId(Long battleId) {
        this.battleId = battleId;
    }
}
