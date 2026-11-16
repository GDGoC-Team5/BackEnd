package com.gdgoc.studyhub.domain.study.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record StudyCreateRequest(
        @NotBlank
        @Size(max = 50)
        String name,

        @Size(max = 500)
        String description
) {
}
