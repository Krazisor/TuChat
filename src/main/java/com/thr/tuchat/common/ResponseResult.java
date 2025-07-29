package com.thr.tuchat.common;

import com.thr.tuchat.constant.ResultCode;
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

    public static <T> ResponseResult<T> success(String message, T data) {
        return new ResponseResult<>(ResultCode.SUCCESS.getCode(), message, data);
    }

    public static <T> ResponseResult<T> fail() {
        return fail(ResultCode.FAIL.getMessage());
    }

    public static <T> ResponseResult<T> fail(String message) {
        return new ResponseResult<>(ResultCode.FAIL.getCode(), message, null);
    }

    public static <T> ResponseResult<T> fail(int code, String message) {
        return new ResponseResult<>(code, message, null);
    }

    public static <T> ResponseResult<T> fail(ResultCode resultCode) {
        return new ResponseResult<>(resultCode.getCode(), resultCode.getMessage(), null);
    }

    // ====== 链式调用 ======
    public ResponseResult<T> code(int code) {
        this.setCode(code);
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
