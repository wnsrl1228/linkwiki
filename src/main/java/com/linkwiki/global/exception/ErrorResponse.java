package com.linkwiki.global.exception;

import lombok.Getter;

@Getter
public class ErrorResponse {

    private int code;
    private String message;

    public ErrorResponse(final int code, final String message) {
        this.code = code;
        this.message = message;
    }

    public ErrorResponse(final ErrorCode errorCode) {
        this.code = errorCode.getCode();
        this.message = errorCode.getMessage();
    }
}