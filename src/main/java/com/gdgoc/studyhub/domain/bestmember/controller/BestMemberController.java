package com.gdgoc.studyhub.domain.bestmember.controller;

import com.gdgoc.studyhub.domain.bestmember.dto.BestMemberCreateRequest;
import com.gdgoc.studyhub.domain.bestmember.dto.BestMemberResponse;
import com.gdgoc.studyhub.domain.bestmember.dto.BestMemberUpdateRequest;
import com.gdgoc.studyhub.domain.bestmember.service.BestMemberService;
import com.gdgoc.studyhub.global.common.ApiResponse;
import com.gdgoc.studyhub.global.security.CustomUserDetails;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/**
 * 모든 API 는 로그인 필요. 선정·수정·취소는 주차가 속한 스터디의 MANAGER 만 할 수 있다.
 */
@RestController
@RequestMapping("/api/weeks/{weekId}/best-member")
@RequiredArgsConstructor
public class BestMemberController {

    private final BestMemberService bestMemberService;

    /** 로그인한 사람은 누구나 조회 가능. 선정 전이면 data 가 null. */
    @GetMapping
    public ApiResponse<BestMemberResponse> get(@PathVariable Long weekId) {
        return ApiResponse.ok(bestMemberService.get(weekId));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<BestMemberResponse> select(@AuthenticationPrincipal CustomUserDetails principal,
                                                  @PathVariable Long weekId,
                                                  @Valid @RequestBody BestMemberCreateRequest request) {
        return ApiResponse.ok(bestMemberService.select(weekId, principal.getUserId(), request));
    }

    @PatchMapping
    public ApiResponse<BestMemberResponse> update(@AuthenticationPrincipal CustomUserDetails principal,
                                                  @PathVariable Long weekId,
                                                  @Valid @RequestBody BestMemberUpdateRequest request) {
        return ApiResponse.ok(bestMemberService.update(weekId, principal.getUserId(), request));
    }

    @DeleteMapping
    public ApiResponse<Void> cancel(@AuthenticationPrincipal CustomUserDetails principal,
                                    @PathVariable Long weekId) {
        bestMemberService.cancel(weekId, principal.getUserId());
        return ApiResponse.ok();
    }
}