package com.linkwiki.global.exception;

import lombok.Getter;

@Getter
public class AuthException extends RuntimeException {
    private final int code;
    private final String message;

    public AuthException(final ErrorCode errorCode) {
        this.code = errorCode.getCode();
        this.message = errorCode.getMessage();
    }
}