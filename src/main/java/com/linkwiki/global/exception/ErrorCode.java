package com.linkwiki.global.exception;

import lombok.Getter;

@Getter
public enum ErrorCode {

    INVALID_REQUEST(1111, "잘못된 요청입니다."),
    SERVER_ERROR(9999, "서버에 문제가 발생하였습니다."),

    FAILED_TO_LOGIN(1000, "아이디와 패스워드가 일치하지 않습니다."),
    DUPLICATE_USERID(1010, "이미 존재하는 아이디입니다."),
    DUPLICATE_NICKNAME(1011, "이미 존재하는 닉네임입니다."),

    INVALID_TOKEN(4002, "유효하지 않은 토큰입니다."),
    EXPIRED_TOKEN(4003, "만료된 토큰입니다."),
    INVALID_REFRESH_TOKEN(4004, "로그인 유효기간이 만료되었습니다"),

    INVALID_JWT_FORMAT(5001, "유효하지 않은 JWT 형식입니다."),

    INVALID_SEARCH_REQUEST(6000, "잘못된 형식의 검색 요청입니다."),


    NOT_FOUND_MEMBER_ID(7000, "해당 멤버를 찾을 수 없습니다.");
    private final int code;
    private final String message;

    ErrorCode(int code, String message) {
        this.code = code;
        this.message = message;
    }
}
