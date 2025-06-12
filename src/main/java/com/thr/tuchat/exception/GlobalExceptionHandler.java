package com.thr.tuchat.exception;

import com.thr.tuchat.pojo.ResponseResult;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(Exception.class)
    public ResponseResult<String> handleException(Exception ex) {
        // 打日志，定制返回
        return ResponseResult.fail("服务异常：" + ex.getMessage());
    }
}
