package com.gdgoc.studyhub.domain.submission.dto;

import jakarta.validation.constraints.NotNull;

public record SubmissionCheckRequest(@NotNull Boolean checked) {
}
