package com.gdgoc.studyhub.domain.auth.exception;

import com.gdgoc.studyhub.global.exception.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum AuthErrorCode implements ErrorCode {

    /** 이메일이 없든 비밀번호가 틀리든 같은 코드로 응답해 가입 여부를 노출하지 않는다. */
    LOGIN_FAILED(HttpStatus.UNAUTHORIZED, "AUTH_401", "이메일 또는 비밀번호가 올바르지 않습니다.");

    private final HttpStatus status;
    private final String code;
    private final String message;
}
