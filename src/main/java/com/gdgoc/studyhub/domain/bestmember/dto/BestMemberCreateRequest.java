package com.gdgoc.studyhub.domain.bestmember.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record BestMemberCreateRequest(
        @NotNull(message = "우수 멤버로 선정할 회원(userId)은 필수입니다.")
        Long userId,

        @Size(max = 500, message = "선정 사유는 500자 이하여야 합니다.")
        String reason
) {
}