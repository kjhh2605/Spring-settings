package com.myApp.global.apiPayload.code.status;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum AuthErrorCode implements BaseCode {

    // Auth (인증/권한 - JWT, Security)
    AUTH_UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "AUTH_401", "인증 정보가 유효하지 않습니다."),
    AUTH_TOKEN_EXPIRED(HttpStatus.UNAUTHORIZED, "AUTH_401_01", "토큰의 유효기간이 만료되었습니다."),
    AUTH_TOKEN_INVALID(HttpStatus.UNAUTHORIZED, "AUTH_401_02", "잘못된 토큰입니다."),
    AUTH_TOKEN_NOT_FOUND(HttpStatus.UNAUTHORIZED, "AUTH_401_03", "토큰이 존재하지 않습니다."),
    AUTH_FORBIDDEN(HttpStatus.FORBIDDEN, "AUTH_403", "해당 리소스에 대한 접근 권한이 없습니다."),

    INVALID_REFRESH_TOKEN(HttpStatus.UNAUTHORIZED, "AUTH_401_04", "리프레시 토큰이 유효하지 않습니다."),
    REFRESH_TOKEN_NOT_FOUND(HttpStatus.UNAUTHORIZED, "AUTH_401_05", "쿠키에 리프레시 토큰이 없습니다. 다시 로그인해주세요."),
    REFRESH_TOKEN_EXPIRED(HttpStatus.UNAUTHORIZED, "AUTH_401_06", "리프레시 토큰이 만료되었습니다. 다시 로그인해주세요."),
    REFRESH_TOKEN_MISMATCH(HttpStatus.UNAUTHORIZED, "AUTH_401_07", "DB에 저장된 리프레시 토큰과 일치하지 않습니다. (재로그인 필요)");

    private final HttpStatus httpStatus;
    private final String code;
    private final String message;
}
