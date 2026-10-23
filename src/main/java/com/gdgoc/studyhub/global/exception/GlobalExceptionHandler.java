package com.gdgoc.studyhub.global.exception;

import com.gdgoc.studyhub.global.common.ApiResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

/**
 * 컨트롤러에서 발생한 예외를 공통 응답 포맷(ApiResponse)으로 변환한다.
 * 서버 내부 정보(SQL, 스택트레이스 등)는 응답에 노출하지 않고 로그로만 남긴다.
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ApiResponse<Void>> handleBusinessException(BusinessException e) {
        ErrorCode errorCode = e.getErrorCode();
        log.info("BusinessException: code={}, message={}", errorCode.getCode(), e.getMessage());
        return toResponse(errorCode, ErrorResponse.of(errorCode, e.getMessage()));
    }

    /** @Valid @RequestBody 검증 실패 → 400 + 필드별 사유 */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Void>> handleMethodArgumentNotValid(MethodArgumentNotValidException e) {
        return toResponse(ErrorCode.INVALID_INPUT, ErrorResponse.of(ErrorCode.INVALID_INPUT, e.getBindingResult()));
    }

    /** JSON 파싱 실패, 타입 불일치, 필수 파라미터 누락 → 400 */
    @ExceptionHandler({
            HttpMessageNotReadableException.class,
            MethodArgumentTypeMismatchException.class,
            MissingServletRequestParameterException.class
    })
    public ResponseEntity<ApiResponse<Void>> handleBadRequest(Exception e) {
        log.info("Bad request: {}", e.getMessage());
        return toResponse(ErrorCode.INVALID_INPUT, ErrorResponse.of(ErrorCode.INVALID_INPUT));
    }

    /**
     * DB 제약 조건 위반(주로 UNIQUE) → 409.
     * 서비스의 exists 체크를 동시 요청이 통과해도 DB UNIQUE 제약이 최종 방어선이 된다.
     */
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ApiResponse<Void>> handleDataIntegrityViolation(DataIntegrityViolationException e) {
        log.warn("DataIntegrityViolationException: {}", e.getMostSpecificCause().getMessage());
        return toResponse(ErrorCode.DUPLICATE_RESOURCE, ErrorResponse.of(ErrorCode.DUPLICATE_RESOURCE));
    }

    /**
     * 컨트롤러/서비스 단의 메서드 보안(@PreAuthorize 등)에서 던진 예외.
     * 아래 Exception 핸들러에 잡혀 500 이 되지 않도록 명시적으로 401/403 으로 변환한다.
     */
    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ApiResponse<Void>> handleAuthentication(AuthenticationException e) {
        return toResponse(ErrorCode.UNAUTHORIZED, ErrorResponse.of(ErrorCode.UNAUTHORIZED));
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiResponse<Void>> handleAccessDenied(AccessDeniedException e) {
        return toResponse(ErrorCode.FORBIDDEN, ErrorResponse.of(ErrorCode.FORBIDDEN));
    }

    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<ApiResponse<Void>> handleNoResourceFound(NoResourceFoundException e) {
        return toResponse(ErrorCode.RESOURCE_NOT_FOUND, ErrorResponse.of(ErrorCode.RESOURCE_NOT_FOUND));
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ApiResponse<Void>> handleMethodNotSupported(HttpRequestMethodNotSupportedException e) {
        return toResponse(ErrorCode.METHOD_NOT_ALLOWED, ErrorResponse.of(ErrorCode.METHOD_NOT_ALLOWED));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleException(Exception e) {
        log.error("Unhandled exception", e);
        return toResponse(ErrorCode.INTERNAL_SERVER_ERROR, ErrorResponse.of(ErrorCode.INTERNAL_SERVER_ERROR));
    }

    private ResponseEntity<ApiResponse<Void>> toResponse(ErrorCode errorCode, ErrorResponse errorResponse) {
        return ResponseEntity.status(errorCode.getStatus()).body(ApiResponse.fail(errorResponse));
    }
}
