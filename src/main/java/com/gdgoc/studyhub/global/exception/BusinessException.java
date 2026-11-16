package com.gdgoc.studyhub.global.exception;

import lombok.Getter;

/**
 * 비즈니스 규칙 위반 시 던지는 예외. GlobalExceptionHandler 가 ErrorCode 의 상태 코드로 변환한다.
 * <pre>
 * throw new BusinessException(CommonErrorCode.FORBIDDEN);
 * throw new BusinessException(UserErrorCode.USER_NOT_FOUND);
 * </pre>
 */
@Getter
public class BusinessException extends RuntimeException {

    private final ErrorCode errorCode;

    public BusinessException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
    }

    /** 기본 메시지 대신 상황에 맞는 메시지를 내려줄 때 사용한다. */
    public BusinessException(ErrorCode errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }
}
