package com.suibian.exception;

import cn.dev33.satoken.exception.NotLoginException;
import com.suibian.common.BaseResponse;
import com.suibian.common.ErrorCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * 全局异常处理
 */
@RestControllerAdvice
@Slf4j
// 在接口文档中隐藏
// 这个注解是 swagger3 的，jdk的最低要求要 jdk17 我当前版本是 jdk8 所以就不用这个了
//@Hidden
public class GlobalExceptionHandler {

    @ExceptionHandler(BusinessException.class)
    public BaseResponse<String> BusinessExceptionHandler(BusinessException e) {
        log.error("业务异常BusinessException: ", e);
        return new BaseResponse<>(e.getCode(), null, e.getMsg());
    }

    @ExceptionHandler(NotLoginException.class)
    public BaseResponse<String> NotLoginExceptionHandler(NotLoginException e) {
        log.error("登录异常NotLoginException: ", e);
        return new BaseResponse<>(ErrorCode.UNAUTHORIZED.getCode(), null, e.getMessage());
    }

    @ExceptionHandler(Exception.class)
    public BaseResponse<?> ExceptionHandler(Exception e) {
        log.error("系统异常Exception: ", e);
        return new BaseResponse<>(ErrorCode.INTERNAL_SERVER_ERROR);
    }
}
