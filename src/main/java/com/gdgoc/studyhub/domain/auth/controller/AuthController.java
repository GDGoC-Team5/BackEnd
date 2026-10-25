package com.gdgoc.studyhub.domain.auth.controller;

import com.gdgoc.studyhub.domain.auth.dto.LoginRequest;
import com.gdgoc.studyhub.domain.auth.dto.SignupRequest;
import com.gdgoc.studyhub.domain.auth.service.AuthService;
import com.gdgoc.studyhub.domain.user.dto.UserResponse;
import com.gdgoc.studyhub.domain.user.service.UserService;
import com.gdgoc.studyhub.global.common.ApiResponse;
import com.gdgoc.studyhub.global.security.CustomUserDetails;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.context.SecurityContextHolderStrategy;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/**
 * 로그아웃(POST /api/auth/logout)은 컨트롤러가 아니라 SecurityConfig 의 LogoutFilter 가 처리한다.
 */
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final UserService userService;
    private final SecurityContextRepository securityContextRepository;
    private final SecurityContextHolderStrategy securityContextHolderStrategy =
            SecurityContextHolder.getContextHolderStrategy();

    /** SecurityConfig 에서 permitAll + CSRF 제외로 열어둔 엔드포인트 */
    @PostMapping("/signup")
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<UserResponse> signup(@Valid @RequestBody SignupRequest request) {
        return ApiResponse.ok(authService.signup(request));
    }

    /** SecurityConfig 에서 permitAll + CSRF 제외로 열어둔 엔드포인트 */
    @PostMapping("/login")
    public ApiResponse<UserResponse> login(@Valid @RequestBody LoginRequest request,
                                           HttpServletRequest httpRequest, HttpServletResponse httpResponse) {
        Authentication authentication = authService.authenticate(request);

        // 세션 고정 공격 방어: 로그인 전부터 있던 세션이면 ID 를 새로 발급한다.
        // (공격자가 미리 심어둔 세션 ID 로는 로그인된 세션에 접근할 수 없게 된다)
        if (httpRequest.getSession(false) != null) {
            httpRequest.changeSessionId();
        }

        // 인증 결과를 SecurityContext 에 담아 세션에 저장한다. 이후 요청은 JSESSIONID 쿠키로 이 context 를 다시 찾는다.
        SecurityContext context = securityContextHolderStrategy.createEmptyContext();
        context.setAuthentication(authentication);
        securityContextHolderStrategy.setContext(context);
        securityContextRepository.saveContext(context, httpRequest, httpResponse);

        CustomUserDetails principal = (CustomUserDetails) authentication.getPrincipal();
        return ApiResponse.ok(userService.getUser(principal.getUserId()));
    }
}
