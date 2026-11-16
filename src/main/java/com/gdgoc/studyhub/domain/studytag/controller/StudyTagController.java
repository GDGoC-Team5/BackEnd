package com.gdgoc.studyhub.domain.studytag.controller;

import com.gdgoc.studyhub.domain.studytag.dto.StudyTagCreateRequest;
import com.gdgoc.studyhub.domain.studytag.dto.StudyTagResponse;
import com.gdgoc.studyhub.domain.studytag.service.StudyTagService;
import com.gdgoc.studyhub.global.common.ApiResponse;
import com.gdgoc.studyhub.global.security.CustomUserDetails;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/studies/{studyId}/tags")
@RequiredArgsConstructor
public class StudyTagController {

    private final StudyTagService studyTagService;

    /** MANAGER 만 */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<StudyTagResponse> create(@AuthenticationPrincipal CustomUserDetails principal,
                                                @PathVariable Long studyId,
                                                @Valid @RequestBody StudyTagCreateRequest request) {
        return ApiResponse.ok(studyTagService.create(studyId, principal.getUserId(), request.name()));
    }

    /** 멤버만 */
    @GetMapping
    public ApiResponse<List<StudyTagResponse>> getTags(@AuthenticationPrincipal CustomUserDetails principal,
                                                       @PathVariable Long studyId) {
        return ApiResponse.ok(studyTagService.getTags(studyId, principal.getUserId()));
    }

    /** MANAGER 만 */
    @DeleteMapping("/{tagId}")
    public ApiResponse<Void> delete(@AuthenticationPrincipal CustomUserDetails principal,
                                    @PathVariable Long studyId,
                                    @PathVariable Long tagId) {
        studyTagService.delete(studyId, principal.getUserId(), tagId);
        return ApiResponse.ok();
    }
}
