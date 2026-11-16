package com.gdgoc.studyhub.domain.studymember.dto;

import jakarta.validation.constraints.NotBlank;

public record StudyJoinRequest(
        @NotBlank
        String inviteCode
) {
}
