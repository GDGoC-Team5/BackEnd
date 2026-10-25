package com.gdgoc.studyhub.domain.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

/**
 * 로그인 요청. 비밀번호 규칙(길이 등)은 검사하지 않는다.
 * 규칙에 안 맞으면 어차피 일치하는 회원이 없으므로 401 로 처리되고, 규칙을 응답으로 노출할 필요도 없다.
 */
public record LoginRequest(
        @NotBlank
        @Email
        String email,

        @NotBlank
        String password
) {
}
