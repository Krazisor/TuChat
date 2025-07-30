package com.thr.tuchat.exception;

import com.thr.tuchat.common.ResponseResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * 全局异常处理器
 */
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(BusinessException.class)
    public ResponseResult<?> businessExceptionHandler(BusinessException e) {
        log.error("BusinessException", e);
        return ResponseResult.fail(ResultCode.SYSTEM_ERROR, e.getMessage());
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseResult<?> businessExceptionHandler(RuntimeException e) {
        log.error("RuntimeException", e);
        return ResponseResult.fail(ResultCode.SYSTEM_ERROR, "系统错误");
    }
}
