package com.cardgame.common;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * 全局异常处理器：把各类异常统一转换成 Result 结构，避免每个 Controller 写 try-catch。
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    /** 业务异常：属于可预期的错误，返回对应错误码 */
    @ExceptionHandler(BizException.class)
    public Result<Void> handleBiz(BizException e) {
        return Result.fail(e.getCode(), e.getMessage());
    }

    /** 参数校验异常：@Valid 校验失败 */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public Result<Void> handleValid(MethodArgumentNotValidException e) {
        String msg = e.getBindingResult().getFieldErrors().stream()
                .findFirst()
                .map(f -> f.getField() + " " + f.getDefaultMessage())
                .orElse(ResultCode.PARAM_ERROR.getMessage());
        return Result.fail(ResultCode.PARAM_ERROR, msg);
    }

    /** 非法参数：类型转换失败、缺少必填参数等 */
    @ExceptionHandler({IllegalArgumentException.class, MissingServletRequestParameterException.class})
    public Result<Void> handleIllegal(Exception e) {
        return Result.fail(ResultCode.PARAM_ERROR, e.getMessage());
    }

    /** 兜底：未预期的异常记录日志后返回通用错误 */
    @ExceptionHandler(Exception.class)
    public Result<Void> handleOther(Exception e) {
        log.error("未处理的服务端异常", e);
        return Result.fail(ResultCode.SERVER_ERROR);
    }
}
