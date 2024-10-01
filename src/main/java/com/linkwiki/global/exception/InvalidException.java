package com.linkwiki.global.exception;

import lombok.Getter;

@Getter
public class InvalidException extends RuntimeException {
    private final int code;
    private final String message;

    public InvalidException(final ErrorCode errorCode) {
        this.code = errorCode.getCode();
        this.message = errorCode.getMessage();
    }
}