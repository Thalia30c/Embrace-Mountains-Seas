package com.cardgame.common;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * 全局异常处理。
 *
 * <p>把各类异常统一转成 {@link Result} 格式返回，让小程序端只需判断
 * {@code code === 0}，不必区分「Spring 默认错误页」和「业务失败」。</p>
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    /** 数据库不可用（账号密码不对、MySQL 没启动等） */
    @ExceptionHandler(org.springframework.dao.DataAccessException.class)
    public Result<Void> handleDataAccess(org.springframework.dao.DataAccessException e) {
        log.error("数据库访问失败", e);
        return Result.fail(Result.SERVER_ERROR, "数据库访问失败，请检查数据库配置或后端服务");
    }

    /** 业务层主动抛出的参数错误 */
    @ExceptionHandler(IllegalArgumentException.class)
    public Result<Void> handleIllegalArgument(IllegalArgumentException e) {
        log.warn("参数错误: {}", e.getMessage());
        return Result.fail(Result.PARAM_ERROR, e.getMessage());
    }

    /** 兜底 */
    @ExceptionHandler(Exception.class)
    public Result<Void> handleException(Exception e) {
        log.error("未处理的服务端异常", e);
        return Result.fail(Result.SERVER_ERROR, "服务器内部错误");
    }
}
