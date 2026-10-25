package com.gdgoc.studyhub.domain.user.controller;

import com.gdgoc.studyhub.domain.user.dto.UserResponse;
import com.gdgoc.studyhub.domain.user.service.UserService;
import com.gdgoc.studyhub.global.common.ApiResponse;
import com.gdgoc.studyhub.global.security.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    /**
     * 내 정보 조회. 로그인하지 않았으면 컨트롤러에 오기 전에 시큐리티가 401 을 반환한다.
     * 세션의 principal 에는 userId 만 믿고 쓰고, 이름 등 최신 정보는 DB 에서 다시 읽는다.
     */
    @GetMapping("/me")
    public ApiResponse<UserResponse> me(@AuthenticationPrincipal CustomUserDetails principal) {
        return ApiResponse.ok(userService.getUser(principal.getUserId()));
    }
}
