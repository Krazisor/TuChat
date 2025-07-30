package com.thr.tuchat.common;

import com.thr.tuchat.exception.ResultCode;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

@Setter
@Getter
public class ResponseResult<T> implements Serializable {
    // ====== Getter/Setter ======
    private int code;
    private String message;
    private T data;

    public ResponseResult() {}

    public ResponseResult(int code, String message, T data) {
        this.code = code;
        this.message = message;
        this.data = data;
    }

    // ====== 静态工厂方法 ======
    public static <T> ResponseResult<T> success() {
        return success(null);
    }

    public static <T> ResponseResult<T> success(T data) {
        return new ResponseResult<>(ResultCode.SUCCESS.getCode(), ResultCode.SUCCESS.getMessage(), data);
    }

    public static <T> ResponseResult<T> fail(ResultCode code, String message) {
        return new ResponseResult<>(code.getCode(), message, null);
    }

    public static <T> ResponseResult<T> fail(ResultCode errorCode) {
        return new ResponseResult<>(errorCode.getCode(), errorCode.getMessage(), null);
    }

    // ====== 链式调用 ======
    public ResponseResult<T> code(ResultCode code) {
        this.setCode(code.getCode());
        return this;
    }

    public ResponseResult<T> message(String message) {
        this.setMessage(message);
        return this;
    }

    public ResponseResult<T> data(T data) {
        this.setData(data);
        return this;
    }
}
