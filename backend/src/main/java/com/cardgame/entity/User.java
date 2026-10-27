package com.cardgame.entity;

import com.cardgame.dao.Column;
import com.cardgame.dao.Table;

/**
 * 用户信息表 user 对应的实体。
 *
 * <p>字段：uid / username / user_account / user_pwd / game_level / score / warehouse_id</p>
 */
@Table("user")
public class User {

    /** 用户自增 ID */
    @Column(value = "uid", id = true)
    private Integer uid;

    /** 用户名 */
    @Column("username")
    private String username;

    /** 登录账号，唯一 */
    @Column("user_account")
    private String userAccount;

    /** 登录密码，正式环境建议存密文 */
    @Column("user_pwd")
    private String userPwd;

    /** 游戏等级，关联 game_rank.rank_id */
    @Column("game_level")
    private Integer gameLevel;

    /** 现有积分 */
    @Column("score")
    private Integer score;

    /** 仓库编号，关联 warehouse.warehouse_id */
    @Column("warehouse_id")
    private Integer warehouseId;

    public Integer getUid() { return uid; }
    public void setUid(Integer uid) { this.uid = uid; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getUserAccount() { return userAccount; }
    public void setUserAccount(String userAccount) { this.userAccount = userAccount; }
    public String getUserPwd() { return userPwd; }
    public void setUserPwd(String userPwd) { this.userPwd = userPwd; }
    public Integer getGameLevel() { return gameLevel; }
    public void setGameLevel(Integer gameLevel) { this.gameLevel = gameLevel; }
    public Integer getScore() { return score; }
    public void setScore(Integer score) { this.score = score; }
    public Integer getWarehouseId() { return warehouseId; }
    public void setWarehouseId(Integer warehouseId) { this.warehouseId = warehouseId; }

    @Override
    public String toString() {
        return "User{uid=" + uid + ", username='" + username + "', userAccount='" + userAccount
                + "', gameLevel=" + gameLevel + ", score=" + score + ", warehouseId=" + warehouseId + '}';
    }
}
