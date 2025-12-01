package com.devpath.global.apiPayload.exception.handler;

import com.devpath.global.apiPayload.code.BaseErrorCode;
import com.devpath.global.apiPayload.exception.GeneralException;

/**
 * {@link GeneralException}을 상속받는 사용자 정의 예외 핸들러 클래스입니다.
 * 이 클래스는 특정 에러 코드({@link BaseErrorCode})를 사용하여 예외를 생성할 때 사용됩니다.
 */
public class GlobalHandler extends GeneralException {
    public GlobalHandler(BaseErrorCode code) {
        super(code);
    }
}
