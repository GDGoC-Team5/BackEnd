package com.gdgoc.studyhub.global.common;

import com.gdgoc.studyhub.global.exception.ErrorResponse;

/**
 * 모든 API 의 공통 응답 포맷.
 * <pre>
 * 성공: { "success": true,  "data": {...}, "error": null }
 * 실패: { "success": false, "data": null,  "error": { "code": "...", "message": "...", "fieldErrors": [...] } }
 * </pre>
 */
public record ApiResponse<T>(boolean success, T data, ErrorResponse error) {

    public static <T> ApiResponse<T> ok(T data) {
        return new ApiResponse<>(true, data, null);
    }

    public static ApiResponse<Void> ok() {
        return new ApiResponse<>(true, null, null);
    }

    public static ApiResponse<Void> fail(ErrorResponse error) {
        return new ApiResponse<>(false, null, error);
    }
}
