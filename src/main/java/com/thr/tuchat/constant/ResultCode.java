package com.thr.tuchat.constant;

import lombok.Getter;

@Getter
public enum ResultCode {
    SUCCESS(200, "操作成功"),
    FAIL(500, "操作失败"),
    VALIDATE_FAIL(400, "参数校验失败"),
    UNAUTHORIZED(401, "未认证或token过期"),
    FORBIDDEN(403, "无权限访问");

    private final int code;
    private final String message;

    ResultCode(int code, String message) {
        this.code = code;
        this.message = message;
    }

}
