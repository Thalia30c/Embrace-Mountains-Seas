package com.cardgame.common;

/**
 * 统一响应体，所有接口返回格式一致。
 *
 * <pre>
 * { "code": 0, "msg": "成功", "data": {...}, "timestamp": 1793000000000 }
 * </pre>
 *
 * @param <T> 业务数据类型
 */
public class Result<T> {

    private int code;
    private String msg;
    private T data;
    private long timestamp;

    public Result() {
        this.timestamp = System.currentTimeMillis();
    }

    private Result(int code, String msg, T data) {
        this.code = code;
        this.msg = msg;
        this.data = data;
        this.timestamp = System.currentTimeMillis();
    }

    /** 成功，无返回数据 */
    public static <T> Result<T> ok() {
        return new Result<>(ResultCode.SUCCESS.getCode(), ResultCode.SUCCESS.getMessage(), null);
    }

    /** 成功，带返回数据 */
    public static <T> Result<T> ok(T data) {
        return new Result<>(ResultCode.SUCCESS.getCode(), ResultCode.SUCCESS.getMessage(), data);
    }

    /** 失败，使用枚举中的默认文案 */
    public static <T> Result<T> fail(ResultCode resultCode) {
        return new Result<>(resultCode.getCode(), resultCode.getMessage(), null);
    }

    /** 失败，自定义文案 */
    public static <T> Result<T> fail(ResultCode resultCode, String msg) {
        return new Result<>(resultCode.getCode(), msg, null);
    }

    /** 失败，自定义错误码与文案 */
    public static <T> Result<T> fail(int code, String msg) {
        return new Result<>(code, msg, null);
    }

    public boolean isSuccess() {
        return code == ResultCode.SUCCESS.getCode();
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
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

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }
}
