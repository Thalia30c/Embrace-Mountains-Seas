package com.cardgame.work.model;

import com.cardgame.entity.User;

/**
 * 登录业务结果：success 为布尔判定结果，其余字段是命中用户后带回的信息。
 */
public class LoginResult {

    /** 账号密码是否匹配（true 存在该用户，false 不存在或密码错误） */
    private boolean success;

    /** 结果说明，失败时是失败原因 */
    private String message;

    /** 用户 ID */
    private Integer userId;

    /** 登录账号 */
    private String userAccount;

    /** 用户名（昵称） */
    private String nickname;

    /** 游戏等级编号 */
    private Integer gameLevel;

    /** 段位全称，如 山精三段 */
    private String rank;

    /** 现有积分 */
    private Integer score;

    /** 仓库编号 */
    private Integer warehouseId;

    public LoginResult() {
    }

    /** 登录失败：只有 success = false 与原因 */
    public static LoginResult fail(String message) {
        LoginResult result = new LoginResult();
        result.success = false;
        result.message = message;
        return result;
    }

    /** 登录成功：success = true 并带回用户信息 */
    public static LoginResult ok(User user, String rankName) {
        LoginResult result = new LoginResult();
        result.success = true;
        result.message = "登录成功";
        result.userId = user.getUid();
        result.userAccount = user.getUserAccount();
        result.nickname = user.getUsername();
        result.gameLevel = user.getGameLevel();
        result.rank = rankName;
        result.score = user.getScore();
        result.warehouseId = user.getWarehouseId();
        return result;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Integer getUserId() {
        return userId;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
    }

    public String getUserAccount() {
        return userAccount;
    }

    public void setUserAccount(String userAccount) {
        this.userAccount = userAccount;
    }

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public Integer getGameLevel() {
        return gameLevel;
    }

    public void setGameLevel(Integer gameLevel) {
        this.gameLevel = gameLevel;
    }

    public String getRank() {
        return rank;
    }

    public void setRank(String rank) {
        this.rank = rank;
    }

    public Integer getScore() {
        return score;
    }

    public void setScore(Integer score) {
        this.score = score;
    }

    public Integer getWarehouseId() {
        return warehouseId;
    }

    public void setWarehouseId(Integer warehouseId) {
        this.warehouseId = warehouseId;
    }
}
