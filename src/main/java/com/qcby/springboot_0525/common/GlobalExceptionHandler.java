package com.qcby.springboot_0525.common;

import cn.dev33.satoken.exception.NotLoginException;
import cn.dev33.satoken.exception.NotRoleException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * 全局异常处理器
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    /**
     * 捕获业务异常
     */
    @ExceptionHandler(BusinessException.class)
    public Result<?> handleBusinessException(BusinessException e) {
        return Result.fail(e.getCode(), e.getMessage());
    }

    /**
     * 捕获未登录异常
     */
    @ExceptionHandler(NotLoginException.class)
    public Result<?> handleNotLoginException(NotLoginException e) {
        return Result.fail(401, "未登录或登录已过期");
    }

    /**
     * 捕获无权限异常
     */
    @ExceptionHandler(NotRoleException.class)
    public Result<?> handleNotRoleException(NotRoleException e) {
        return Result.fail(403, "无权限访问");
    }

    /**
     * 捕获其他异常(兜底)
     */
    @ExceptionHandler(Exception.class)
    public Result<?> handleException(Exception e) {
        log.error("系统异常", e);
        return Result.fail("系统异常，请联系管理员");
    }
}
