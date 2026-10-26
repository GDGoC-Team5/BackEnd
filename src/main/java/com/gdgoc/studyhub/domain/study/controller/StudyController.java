package com.gdgoc.studyhub.domain.study.controller;

import com.gdgoc.studyhub.domain.study.dto.MyStudyResponse;
import com.gdgoc.studyhub.domain.study.dto.StudyCreateRequest;
import com.gdgoc.studyhub.domain.study.dto.StudyResponse;
import com.gdgoc.studyhub.domain.study.dto.StudyUpdateRequest;
import com.gdgoc.studyhub.domain.study.service.StudyService;
import com.gdgoc.studyhub.global.common.ApiResponse;
import com.gdgoc.studyhub.global.security.CustomUserDetails;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/**
 * 모든 API 는 로그인 필요 (SecurityConfig 의 anyRequest().authenticated()).
 * 스터디별 권한(멤버/매니저)은 서비스에서 StudyAuthorizationService 로 확인한다.
 */
@RestController
@RequestMapping("/api/studies")
@RequiredArgsConstructor
public class StudyController {

    private final StudyService studyService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<StudyResponse> create(@AuthenticationPrincipal CustomUserDetails principal,
                                             @Valid @RequestBody StudyCreateRequest request) {
        return ApiResponse.ok(studyService.create(principal.getUserId(), request));
    }

    @GetMapping
    public ApiResponse<List<MyStudyResponse>> getMyStudies(@AuthenticationPrincipal CustomUserDetails principal) {
        return ApiResponse.ok(studyService.getMyStudies(principal.getUserId()));
    }

    /** 멤버만 조회 가능 */
    @GetMapping("/{studyId}")
    public ApiResponse<StudyResponse> getStudy(@AuthenticationPrincipal CustomUserDetails principal,
                                               @PathVariable Long studyId) {
        return ApiResponse.ok(studyService.getStudy(studyId, principal.getUserId()));
    }

    /** MANAGER 만 수정 가능 */
    @PatchMapping("/{studyId}")
    public ApiResponse<StudyResponse> update(@AuthenticationPrincipal CustomUserDetails principal,
                                             @PathVariable Long studyId,
                                             @Valid @RequestBody StudyUpdateRequest request) {
        return ApiResponse.ok(studyService.update(studyId, principal.getUserId(), request));
    }
}
