package com.gdgoc.studyhub.domain.submission.controller;

import com.gdgoc.studyhub.domain.submission.dto.SubmissionCheckRequest;
import com.gdgoc.studyhub.domain.submission.dto.SubmissionResponse;
import com.gdgoc.studyhub.domain.submission.dto.SubmissionWriteRequest;
import com.gdgoc.studyhub.domain.submission.service.SubmissionService;
import com.gdgoc.studyhub.global.common.ApiResponse;
import com.gdgoc.studyhub.global.security.CustomUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
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

@RestController
@RequestMapping({"/api", "/api/v1"})
@RequiredArgsConstructor
@Tag(name = "Submission", description = "링크 과제 제출 및 운영진 확인")
public class SubmissionController {

    private final SubmissionService submissionService;

    @PostMapping("/weeks/{weekId}/submissions")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "과제 제출")
    public ApiResponse<SubmissionResponse> create(@AuthenticationPrincipal CustomUserDetails principal,
                                                  @PathVariable Long weekId,
                                                  @Valid @RequestBody SubmissionWriteRequest request) {
        return ApiResponse.ok(submissionService.create(weekId, principal.getUserId(), request));
    }

    @GetMapping("/weeks/{weekId}/submissions")
    @Operation(summary = "주차별 제출물 목록 조회", description = "해당 스터디의 운영진만 조회할 수 있습니다.")
    public ApiResponse<List<SubmissionResponse>> getAll(@AuthenticationPrincipal CustomUserDetails principal,
                                                        @PathVariable Long weekId) {
        return ApiResponse.ok(submissionService.getAll(weekId, principal.getUserId()));
    }

    @GetMapping("/weeks/{weekId}/submissions/me")
    @Operation(summary = "내 제출물 조회")
    public ApiResponse<SubmissionResponse> getMine(@AuthenticationPrincipal CustomUserDetails principal,
                                                   @PathVariable Long weekId) {
        return ApiResponse.ok(submissionService.getMine(weekId, principal.getUserId()));
    }

    @GetMapping("/submissions/{submissionId}")
    @Operation(summary = "제출물 상세 조회", description = "본인 또는 해당 스터디의 운영진만 조회할 수 있습니다.")
    public ApiResponse<SubmissionResponse> get(@AuthenticationPrincipal CustomUserDetails principal,
                                               @PathVariable Long submissionId) {
        return ApiResponse.ok(submissionService.get(submissionId, principal.getUserId()));
    }

    @PatchMapping("/submissions/{submissionId}")
    @Operation(summary = "제출물 수정", description = "본인만 수정할 수 있고, 링크가 바뀌면 확인 상태가 초기화됩니다.")
    public ApiResponse<SubmissionResponse> update(@AuthenticationPrincipal CustomUserDetails principal,
                                                  @PathVariable Long submissionId,
                                                  @Valid @RequestBody SubmissionWriteRequest request) {
        return ApiResponse.ok(submissionService.update(submissionId, principal.getUserId(), request));
    }

    @DeleteMapping("/submissions/{submissionId}")
    @Operation(summary = "제출물 삭제", description = "본인만 삭제할 수 있습니다.")
    public ApiResponse<Void> delete(@AuthenticationPrincipal CustomUserDetails principal,
                                    @PathVariable Long submissionId) {
        submissionService.delete(submissionId, principal.getUserId());
        return ApiResponse.ok();
    }

    @PatchMapping("/submissions/{submissionId}/check")
    @Operation(summary = "제출 확인 처리", description = "운영진만 확인 또는 확인 취소할 수 있습니다.")
    public ApiResponse<SubmissionResponse> check(@AuthenticationPrincipal CustomUserDetails principal,
                                                 @PathVariable Long submissionId,
                                                 @Valid @RequestBody SubmissionCheckRequest request) {
        return ApiResponse.ok(submissionService.check(submissionId, principal.getUserId(), request.checked()));
    }
}
