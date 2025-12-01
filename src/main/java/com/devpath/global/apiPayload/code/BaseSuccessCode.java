package com.devpath.global.apiPayload.code;

import org.springframework.http.HttpStatus;

/**
 * 성공 코드를 나타내는 최상위 인터페이스입니다.
 * 이 인터페이스를 구현하는 모든 성공 코드는 HTTP 상태, 고유 코드, 메시지를 제공해야 합니다.
 */
public interface BaseSuccessCode {

    /**
     * 성공에 해당하는 HTTP 상태를 반환합니다.
     *
     * @return {@link HttpStatus}
     */
    HttpStatus getHttpStatus();

    /**
     * 성공을 식별하는 고유한 코드 문자열을 반환합니다.
     *
     * @return 성공 코드
     */
    String getCode();

    /**
     * 성공에 대한 설명 메시지를 반환합니다.
     *
     * @return 성공 메시지
     */
    String getMessage();
}