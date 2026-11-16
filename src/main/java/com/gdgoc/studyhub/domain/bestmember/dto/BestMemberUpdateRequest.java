package com.gdgoc.studyhub.domain.bestmember.dto;

import jakarta.validation.constraints.Size;

/** 보낸 필드만 수정한다. */
public record BestMemberUpdateRequest(
        Long userId,

        @Size(max = 500, message = "선정 사유는 500자 이하여야 합니다.")
        String reason
) {
}