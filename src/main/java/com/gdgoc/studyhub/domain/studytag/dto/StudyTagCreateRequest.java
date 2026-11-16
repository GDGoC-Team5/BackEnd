package com.gdgoc.studyhub.domain.studytag.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record StudyTagCreateRequest(
        @NotBlank
        @Size(max = 30)
        String name
) {
}
