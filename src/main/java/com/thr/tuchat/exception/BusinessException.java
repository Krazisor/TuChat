package com.thr.tuchat.exception;

import lombok.Getter;

@Getter
public class BusinessException extends RuntimeException{

    /**
     * 错误码
     */
    private final int code;

    public BusinessException(int code, String message) {
        super(message);
        this.code = code;
    }

    public BusinessException(ResultCode errorCode) {
        super(errorCode.getMessage());
        this.code = errorCode.getCode();
    }

    public BusinessException(ResultCode errorCode, String message) {
        super(message);
        this.code = errorCode.getCode();
    }
}
