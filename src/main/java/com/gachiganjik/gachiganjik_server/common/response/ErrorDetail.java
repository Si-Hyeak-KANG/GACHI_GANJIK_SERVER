package com.gachiganjik.gachiganjik_server.common.response;

import lombok.Getter;

@Getter
public class ErrorDetail {

    private final String code;
    private final String message;

    private ErrorDetail(String code, String message) {
        this.code = code;
        this.message = message;
    }

    public static ErrorDetail of(com.gachiganjik.gachiganjik_server.common.exception.ErrorCode errorCode) {
        return new ErrorDetail(errorCode.getCode(), errorCode.getMessage());
    }

    public static ErrorDetail of(String code, String message) {
        return new ErrorDetail(code, message);
    }
}