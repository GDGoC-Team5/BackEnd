package com.gdgoc.studyhub.domain.submission.dto;

import com.gdgoc.studyhub.domain.submission.entity.Submission;
import java.time.LocalDateTime;

public record SubmissionResponse(Long id, Long weekId, Long memberId, Long userId, String name,
                                 String url, boolean checked, LocalDateTime submittedAt,
                                 LocalDateTime updatedAt) {

    public static SubmissionResponse from(Submission submission) {
        return new SubmissionResponse(submission.getId(), submission.getWeek().getId(),
                submission.getMember().getId(), submission.getMember().getUser().getId(),
                submission.getMember().getUser().getName(), submission.getUrl(), submission.isChecked(),
                submission.getCreatedAt(), submission.getUpdatedAt());
    }
}
