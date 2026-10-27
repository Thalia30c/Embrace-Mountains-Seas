package com.cardgame.common;

/**
 * 统一接口返回结构，与小程序 utils/request.js 的约定一致：
 * code = 0 表示成功，非 0 表示业务失败；data 为业务数据。
 *
 * @param <T> 业务数据类型
 */
public class Result<T> {

    /** 成功 */
    public static final int SUCCESS = 0;
    /** 参数错误 */
    public static final int PARAM_ERROR = 40001;
    /** 登录失败（账号不存在或密码错误） */
    public static final int LOGIN_FAIL = 40101;

    private int code;
    private String msg;
    private T data;
    private boolean success;
    private long timestamp = System.currentTimeMillis();

    public Result() {
    }

    public Result(int code, String msg, T data) {
        this.code = code;
        this.msg = msg;
        this.data = data;
        this.success = code == SUCCESS;
    }

    public static <T> Result<T> ok(T data) {
        return new Result<>(SUCCESS, "success", data);
    }

    public static <T> Result<T> ok(String msg, T data) {
        return new Result<>(SUCCESS, msg, data);
    }

    public static <T> Result<T> fail(int code, String msg) {
        return new Result<>(code, msg, null);
    }

    public static <T> Result<T> fail(int code, String msg, T data) {
        return new Result<>(code, msg, data);
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
        this.success = code == SUCCESS;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }
}
