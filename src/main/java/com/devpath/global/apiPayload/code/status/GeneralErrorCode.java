package com.devpath.global.apiPayload.code.status;

import com.devpath.global.apiPayload.code.BaseErrorCode;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

/**
 * 일반적인 에러 코드를 정의하는 열거형입니다.
 * {@link BaseErrorCode}를 구현하여 HTTP 상태, 코드, 메시지를 제공합니다.
 */
@Getter
@AllArgsConstructor
public enum GeneralErrorCode implements BaseErrorCode {

    // 사용자 프로필
    DUPLICATE_EMAIL(HttpStatus.CONFLICT, "USER_409", "이미 사용 중인 이메일입니다"),

    // 명함 교환
    FOLLOW_ALREADY_EXISTED(HttpStatus.BAD_REQUEST,"FOLLOW_400","이미 추가한 명함입니다"),

    // 기본 에러
    _BAD_REQUEST(HttpStatus.BAD_REQUEST, "COMMON_400", "잘못된 요청입니다."),
    _UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "COMMON_401", "인증이 필요합니다."),
    _FORBIDDEN(HttpStatus.FORBIDDEN, "COMMON_403", "금지된 요청입니다."),
    _NO_RESULTS_FOUND(HttpStatus.NOT_FOUND, "COMMON_404", "검색 결과가 없습니다."),
    _INVALID_INPUT(HttpStatus.BAD_REQUEST,"COMMON_405","입력값이 올바르지 않습니다."),
    _INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "COMMON_500", "서버 에러. 관리자에게 문의 바랍니다."),;

    /**
     * HTTP 상태 코드
     */
    private final HttpStatus httpStatus;

    /**
     * 에러 코드
     */
    private final String code;

    /**
     * 에러 메시지
     */
    private final String message;
}