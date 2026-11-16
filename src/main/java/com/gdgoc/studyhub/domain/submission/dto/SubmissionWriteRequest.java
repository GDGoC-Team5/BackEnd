package com.gdgoc.studyhub.domain.submission.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import org.hibernate.validator.constraints.URL;

public record SubmissionWriteRequest(
        @NotBlank
        @Size(max = 2048)
        @URL
        @Pattern(regexp = "(?i)^https?://\\S+$", message = "HTTP 또는 HTTPS 링크를 입력해 주세요.")
        String url
) {
    public SubmissionWriteRequest {
        if (url != null) {
            url = url.trim();
        }
    }
}
