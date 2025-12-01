package com.devpath.global.apiPayload.exception;

import com.devpath.global.apiPayload.code.BaseErrorCode;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 비즈니스 로직 실행 중 발생하는 예외를 표현하는 클래스입니다.
 * {@link BaseErrorCode}를 포함하여, 예외에 대한 구체적인 정보를 제공합니다.
 */
@Getter
@AllArgsConstructor
public class GeneralException extends RuntimeException{
    private final BaseErrorCode code;
}
