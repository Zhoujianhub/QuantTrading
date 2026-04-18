package com.quant.common.result;

public enum ResultCode {
    SUCCESS(200, "success"),
    FAIL(500, "fail"),
    PARAM_ERROR(400, "param error"),
    NOT_FOUND(404, "not found"),
    UNAUTHORIZED(401, "unauthorized"),
    FORBIDDEN(403, "forbidden"),
    INTERNAL_ERROR(500, "internal error");

    private final int code;
    private final String message;

    ResultCode(int code, String message) {
        this.code = code;
        this.message = message;
    }

    public int getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }
}
