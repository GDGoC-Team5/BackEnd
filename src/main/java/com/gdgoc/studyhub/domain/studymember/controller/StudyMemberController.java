package com.gdgoc.studyhub.domain.studymember.controller;

import com.gdgoc.studyhub.domain.study.dto.StudyResponse;
import com.gdgoc.studyhub.domain.studymember.dto.StudyJoinRequest;
import com.gdgoc.studyhub.domain.studymember.dto.StudyMemberResponse;
import com.gdgoc.studyhub.domain.studymember.service.StudyMemberService;
import com.gdgoc.studyhub.global.common.ApiResponse;
import com.gdgoc.studyhub.global.security.CustomUserDetails;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/studies")
@RequiredArgsConstructor
public class StudyMemberController {

    private final StudyMemberService studyMemberService;

    /** 초대 코드로 가입. 로그인만 하면 누구나 가능 */
    @PostMapping("/join")
    public ApiResponse<StudyResponse> join(@AuthenticationPrincipal CustomUserDetails principal,
                                           @Valid @RequestBody StudyJoinRequest request) {
        return ApiResponse.ok(studyMemberService.join(principal.getUserId(), request.inviteCode()));
    }

    /** 멤버만 조회 가능 */
    @GetMapping("/{studyId}/members")
    public ApiResponse<List<StudyMemberResponse>> getMembers(@AuthenticationPrincipal CustomUserDetails principal,
                                                             @PathVariable Long studyId) {
        return ApiResponse.ok(studyMemberService.getMembers(studyId, principal.getUserId()));
    }

    /** 내 탈퇴 */
    @DeleteMapping("/{studyId}/members/me")
    public ApiResponse<Void> leave(@AuthenticationPrincipal CustomUserDetails principal,
                                   @PathVariable Long studyId) {
        studyMemberService.leave(studyId, principal.getUserId());
        return ApiResponse.ok();
    }
}
