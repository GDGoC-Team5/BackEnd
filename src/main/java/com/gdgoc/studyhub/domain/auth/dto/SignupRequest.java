package com.gdgoc.studyhub.domain.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * 회원가입 요청.
 * 비밀번호는 공백 없는 영문·숫자·특수문자 8~64자로 제한한다.
 * BCrypt 는 72바이트까지만 처리(초과 시 예외)하므로, 한글처럼 여러 바이트인 문자를 막아 길이를 보장한다.
 */
public record SignupRequest(
        @NotBlank
        @Email
        @Size(max = 255)
        String email,

        @NotBlank
        @Pattern(regexp = "^[\\x21-\\x7E]{8,64}$", message = "비밀번호는 공백 없는 영문·숫자·특수문자 8~64자여야 합니다.")
        String password,

        @NotBlank
        @Size(max = 20)
        String name
) {
}
