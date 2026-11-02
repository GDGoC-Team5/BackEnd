package com.gdgoc.studyhub.domain.week.controller;

import com.gdgoc.studyhub.domain.week.dto.WeekCreateRequest;
import com.gdgoc.studyhub.domain.week.dto.WeekResponse;
import com.gdgoc.studyhub.domain.week.dto.WeekSummaryResponse;
import com.gdgoc.studyhub.domain.week.dto.WeekUpdateRequest;
import com.gdgoc.studyhub.domain.week.service.WeekService;
import com.gdgoc.studyhub.global.common.ApiResponse;
import com.gdgoc.studyhub.global.security.CustomUserDetails;
import jakarta.validation.Valid;
import java.util.List;
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
 * 모든 API 는 로그인 필요. 스터디별 권한(멤버/매니저)은 서비스에서 StudyAuthorizationService 로 확인한다.
 */
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class WeekController {

    private final WeekService weekService;

    /** MANAGER 만 생성 가능 */
    @PostMapping("/studies/{studyId}/weeks")
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<WeekResponse> create(@AuthenticationPrincipal CustomUserDetails principal,
                                            @PathVariable Long studyId,
                                            @Valid @RequestBody WeekCreateRequest request) {
        return ApiResponse.ok(weekService.create(studyId, principal.getUserId(), request));
    }

    /** 스터디 멤버만 조회 가능 */
    @GetMapping("/studies/{studyId}/weeks")
    public ApiResponse<List<WeekSummaryResponse>> getWeeks(@AuthenticationPrincipal CustomUserDetails principal,
                                                           @PathVariable Long studyId) {
        return ApiResponse.ok(weekService.getWeeks(studyId, principal.getUserId()));
    }

    /** 주차가 속한 스터디의 멤버만 조회 가능 */
    @GetMapping("/weeks/{weekId}")
    public ApiResponse<WeekResponse> getWeek(@AuthenticationPrincipal CustomUserDetails principal,
                                             @PathVariable Long weekId) {
        return ApiResponse.ok(weekService.getWeek(weekId, principal.getUserId()));
    }

    /** MANAGER 만 수정 가능. 보낸 필드만 바뀐다. */
    @PatchMapping("/weeks/{weekId}")
    public ApiResponse<WeekResponse> update(@AuthenticationPrincipal CustomUserDetails principal,
                                            @PathVariable Long weekId,
                                            @Valid @RequestBody WeekUpdateRequest request) {
        return ApiResponse.ok(weekService.update(weekId, principal.getUserId(), request));
    }

    /** MANAGER 만 삭제 가능 */
    @DeleteMapping("/weeks/{weekId}")
    public ApiResponse<Void> delete(@AuthenticationPrincipal CustomUserDetails principal,
                                    @PathVariable Long weekId) {
        weekService.delete(weekId, principal.getUserId());
        return ApiResponse.ok();
    }
}