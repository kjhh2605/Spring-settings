package com.myApp.global.apiPayload.exception.handler;

import com.myApp.global.apiPayload.code.status.BaseCode;
import com.myApp.global.apiPayload.exception.GeneralException;

/**
 * {@link GeneralException}을 상속받는 사용자 정의 예외 핸들러 클래스입니다.
 * 이 클래스는 특정 에러 코드({@link BaseCode})를 사용하여 예외를 생성할 때 사용됩니다.
 */
public class GlobalHandler extends GeneralException {
    public GlobalHandler(BaseCode code) {
        super(code);
    }
}
