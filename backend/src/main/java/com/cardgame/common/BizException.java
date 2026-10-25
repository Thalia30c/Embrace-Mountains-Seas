package com.cardgame.common;

/**
 * 业务异常。Service 层校验不通过时抛出，由全局异常处理器统一转成 Result。
 */
public class BizException extends RuntimeException {

    private final int code;

    public BizException(ResultCode resultCode) {
        super(resultCode.getMessage());
        this.code = resultCode.getCode();
    }

    public BizException(ResultCode resultCode, String msg) {
        super(msg);
        this.code = resultCode.getCode();
    }

    public int getCode() {
        return code;
    }
}
