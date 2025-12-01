package com.devpath.global.apiPayload.exception;

import com.devpath.global.apiPayload.ApiResponse;
import com.devpath.global.apiPayload.code.BaseErrorCode;
import com.devpath.global.apiPayload.code.status.GeneralErrorCode;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

/**
 * @RestControllerAdvice 어노테이션을 사용하여 모든 @RestController 에서 발생하는 예외를 전역적으로 처리하는 클래스입니다.
 */
@Slf4j
@RestControllerAdvice(annotations = {RestController.class})
public class ExceptionAdvice extends ResponseEntityExceptionHandler {

    /**
     * @Valid 어노테이션을 통한 검증 실패 시 발생하는 {@link ConstraintViolationException}을 처리합니다.
     * 주로 @RequestParam, @PathVariable 에서 검증 실패 시 발생합니다.
     *
     * @param e       발생한 {@link ConstraintViolationException}
     * @param request 현재 요청 객체
     * @return 에러 응답 {@link ResponseEntity}
     */
    @ExceptionHandler
    public ResponseEntity<Object> validation(ConstraintViolationException e, WebRequest request) {
        String errorMessage = e.getConstraintViolations().stream()
                .map(ConstraintViolation::getMessage)
                .findFirst()
                .orElseThrow(() -> new RuntimeException("ConstraintViolationException 추출 도중 에러 발생"));

        return handleExceptionInternalConstraint(e, GeneralErrorCode.valueOf(errorMessage), HttpHeaders.EMPTY, request);
    }

    /**
     * @Valid 어노테이션을 통한 검증 실패 시 발생하는 {@link MethodArgumentNotValidException}을 처리합니다.
     * 주로 @RequestBody 에서 검증 실패 시 발생합니다.
     *
     * @param e       발생한 {@link MethodArgumentNotValidException}
     * @param headers 응답에 포함될 HTTP 헤더
     * @param status  응답의 HTTP 상태 코드
     * @param request 현재 요청 객체
     * @return 에러 응답 {@link ResponseEntity}
     */
    @Override
    public ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException e, HttpHeaders headers,
                                                               HttpStatusCode status, WebRequest request) {
        Map<String, String> errors = new LinkedHashMap<>();

        e.getBindingResult().getFieldErrors()
                .forEach(fieldError -> {
                    String fieldName = fieldError.getField();
                    String errorMessage = Optional.ofNullable(fieldError.getDefaultMessage()).orElse("");
                    errors.merge(fieldName, errorMessage,
                            (existingErrorMessage, newErrorMessage) -> existingErrorMessage + ", " + newErrorMessage);
                });

        return handleExceptionInternalArgs(e, HttpHeaders.EMPTY, GeneralErrorCode.valueOf("_BAD_REQUEST"), request,
                errors);
    }

    /**
     * JSON Request Body 파싱 실패 시 발생하는 {@link HttpMessageNotReadableException}을 처리합니다.
     * 주로 잘못된 형식의 JSON이나 Enum 타입 불일치 시 발생합니다.
     *
     * @param e       발생한 {@link HttpMessageNotReadableException}
     * @param headers 응답에 포함될 HTTP 헤더
     * @param status  응답의 HTTP 상태 코드
     * @param request 현재 요청 객체
     * @return 에러 응답 {@link ResponseEntity}
     */
    @Override
    public ResponseEntity<Object> handleHttpMessageNotReadable(HttpMessageNotReadableException e, HttpHeaders headers,
                                                               HttpStatusCode status, WebRequest request) {
        return handleExceptionInternalConstraint(e, GeneralErrorCode._INVALID_INPUT, headers, request);
    }

    /**
     * 처리되지 않은 모든 예외를 처리합니다.
     * 500 Internal Server Error를 반환합니다.
     *
     * @param e       발생한 {@link Exception}
     * @param request 현재 요청 객체
     * @return 에러 응답 {@link ResponseEntity}
     */
    @ExceptionHandler
    public ResponseEntity<Object> exception(Exception e, WebRequest request) {
        log.error("500 Error", e);
        return handleExceptionInternalFalse(e, GeneralErrorCode._INTERNAL_SERVER_ERROR.getHttpStatus(), request,
                e.getMessage());
    }

    /**
     * 비즈니스 로직 실행 중 발생하는 커스텀 예외 {@link GeneralException}을 처리합니다.
     *
     * @param generalException 발생한 {@link GeneralException}
     * @param request          현재 요청 객체
     * @return 에러 응답 {@link ResponseEntity}
     */
    @ExceptionHandler(value = GeneralException.class)
    public ResponseEntity onThrowException(GeneralException generalException, HttpServletRequest request) {
        return handleExceptionInternal(generalException,
                generalException.getCode(),
                HttpHeaders.EMPTY,
                request);
    }

    /**
     * 예외 처리를 위한 중앙 집중식 메서드입니다.
     * {@link ApiResponse} 형태로 에러 응답을 생성합니다.
     *
     * @param e       발생한 {@link Exception}
     * @param reason  에러 코드와 메시지를 담고 있는 {@link BaseErrorCode}
     * @param headers 응답에 포함될 HTTP 헤더
     * @param request 현재 요청 객체
     * @return 에러 응답 {@link ResponseEntity}
     */
    private ResponseEntity<Object> handleExceptionInternal(Exception e, BaseErrorCode reason,
                                                           HttpHeaders headers, HttpServletRequest request) {

        ApiResponse<Object> body = ApiResponse.onFailure(reason, reason.getMessage());

        WebRequest webRequest = new ServletWebRequest(request);
        return super.handleExceptionInternal(
                e,
                body,
                headers,
                reason.getHttpStatus(),
                webRequest);
    }

    /**
     * 500 Internal Server Error 발생 시, 에러 응답을 생성하는 헬퍼 메서드입니다.
     *
     * @param e          발생한 {@link Exception}
     * @param status     응답의 HTTP 상태
     * @param request    현재 요청 객체
     * @param errorPoint 에러 발생 지점
     * @return 에러 응답 {@link ResponseEntity}
     */
    private ResponseEntity<Object> handleExceptionInternalFalse(Exception e,
                                                                HttpStatus status, WebRequest request, String errorPoint) {
        ApiResponse<Object> body = ApiResponse.onFailure(GeneralErrorCode._INTERNAL_SERVER_ERROR, errorPoint);
        return super.handleExceptionInternal(
                e,
                body,
                HttpHeaders.EMPTY,
                status,
                request);
    }

    /**
     * 잘못된 인자 값으로 인한 예외 발생 시, 에러 응답을 생성하는 헬퍼 메서드입니다.
     *
     * @param e                 발생한 {@link Exception}
     * @param headers           응답에 포함될 HTTP 헤더
     * @param errorCommonStatus 에러 코드와 메시지를 담고 있는 {@link GeneralErrorCode}
     * @param request           현재 요청 객체
     * @param errorArgs         에러와 관련된 인자 정보
     * @return 에러 응답 {@link ResponseEntity}
     */
    private ResponseEntity<Object> handleExceptionInternalArgs(Exception e, HttpHeaders headers,
                                                               GeneralErrorCode errorCommonStatus,
                                                               WebRequest request, Map<String, String> errorArgs) {
        ApiResponse<Object> body = ApiResponse.onFailure(errorCommonStatus, errorArgs);
        return super.handleExceptionInternal(
                e,
                body,
                headers,
                errorCommonStatus.getHttpStatus(),
                request);
    }

    /**
     * 제약 조건 위반으로 인한 예외 발생 시, 에러 응답을 생성하는 헬퍼 메서드입니다.
     *
     * @param e                 발생한 {@link Exception}
     * @param errorCommonStatus 에러 코드와 메시지를 담고 있는 {@link GeneralErrorCode}
     * @param headers           응답에 포함될 HTTP 헤더
     * @param request           현재 요청 객체
     * @return 에러 응답 {@link ResponseEntity}
     */
    private ResponseEntity<Object> handleExceptionInternalConstraint(Exception e, GeneralErrorCode errorCommonStatus,
                                                                     HttpHeaders headers, WebRequest request) {
        ApiResponse<Object> body = ApiResponse.onFailure(errorCommonStatus, null);
        return super.handleExceptionInternal(
                e,
                body,
                headers,
                errorCommonStatus.getHttpStatus(),
                request);
    }
}
