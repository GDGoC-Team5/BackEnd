package com.gdgoc.studyhub.global.exception;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.util.List;
import org.springframework.validation.BindingResult;

/**
 * ApiResponse 의 error 필드. fieldErrors 는 @Valid 검증 실패일 때만 포함된다.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record ErrorResponse(String code, String message, List<FieldError> fieldErrors) {

    public static ErrorResponse of(ErrorCode errorCode) {
        return new ErrorResponse(errorCode.getCode(), errorCode.getMessage(), null);
    }

    public static ErrorResponse of(ErrorCode errorCode, String message) {
        return new ErrorResponse(errorCode.getCode(), message, null);
    }

    public static ErrorResponse of(ErrorCode errorCode, BindingResult bindingResult) {
        List<FieldError> fieldErrors = bindingResult.getFieldErrors().stream()
                .map(e -> new FieldError(e.getField(), e.getDefaultMessage()))
                .toList();
        return new ErrorResponse(errorCode.getCode(), errorCode.getMessage(), fieldErrors);
    }

    public record FieldError(String field, String reason) {
    }
}
