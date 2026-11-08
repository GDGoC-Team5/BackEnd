package com.gdgoc.studyhub.domain.material.exception;

import com.gdgoc.studyhub.global.exception.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum MaterialErrorCode implements ErrorCode {

    MATERIAL_NOT_FOUND(HttpStatus.NOT_FOUND, "MATERIAL_404", "세션 자료를 찾을 수 없습니다.");

    private final HttpStatus status;
    private final String code;
    private final String message;
}
