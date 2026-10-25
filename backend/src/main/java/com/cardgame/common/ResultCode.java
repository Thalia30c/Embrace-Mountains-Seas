package com.cardgame.common;

/**
 * 统一响应码。
 *
 * <p>约定：0 表示成功；4xxxx 为客户端/业务错误；5xxxx 为服务端错误。</p>
 */
public enum ResultCode {

    SUCCESS(0, "成功"),

    PARAM_ERROR(40001, "参数校验失败"),
    UNAUTHORIZED(40002, "未登录或登录已过期"),
    FORBIDDEN(40003, "没有操作权限"),
    NOT_FOUND(40004, "资源不存在"),
    CONFLICT(40005, "资源冲突"),

    USER_EXISTS(40006, "用户名已存在"),
    LOGIN_FAILED(40007, "用户名或密码错误"),
    ROOM_FULL(40008, "房间人数已满"),
    NOT_IN_ROOM(40009, "你不在该房间中"),
    NOT_ROOM_OWNER(40010, "只有房主可以执行该操作"),
    NOT_YOUR_TURN(40011, "还没轮到你出牌"),
    CARD_NOT_ENOUGH(40012, "卡牌数量不足"),
    COIN_NOT_ENOUGH(40013, "金币不足"),
    DECK_EMPTY(40014, "牌组不能为空"),
    CARD_ALREADY_IN_DECK(40015, "卡牌已在牌组中"),
    BATTLE_FINISHED(40016, "对局已结束"),

    SERVER_ERROR(50000, "服务器内部错误");

    private final int code;
    private final String message;

    ResultCode(int code, String message) {
        this.code = code;
        this.message = message;
    }

    public int getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }
}
