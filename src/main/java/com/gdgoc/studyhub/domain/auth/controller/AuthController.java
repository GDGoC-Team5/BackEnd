package com.gdgoc.studyhub.domain.auth.controller;

import com.gdgoc.studyhub.domain.auth.dto.SignupRequest;
import com.gdgoc.studyhub.domain.auth.service.AuthService;
import com.gdgoc.studyhub.domain.user.dto.UserResponse;
import com.gdgoc.studyhub.global.common.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    /** SecurityConfig 에서 permitAll + CSRF 제외로 열어둔 엔드포인트 */
    @PostMapping("/signup")
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<UserResponse> signup(@Valid @RequestBody SignupRequest request) {
        return ApiResponse.ok(authService.signup(request));
    }
}
