package com.gdgoc.studyhub.domain.bestmember.dto;

import com.gdgoc.studyhub.domain.bestmember.entity.WeeklyBestMember;
import com.gdgoc.studyhub.domain.study.entity.Study;
import com.gdgoc.studyhub.domain.user.entity.User;
import com.gdgoc.studyhub.domain.week.entity.Week;
import java.time.LocalDateTime;

public record BestMemberResponse(
        Long id,
        Long studyId,
        String studyName,
        Long weekId,
        int weekNo,
        String weekTitle,
        Long userId,
        String name,
        String reason,
        LocalDateTime selectedAt,
        LocalDateTime updatedAt
) {

    public static BestMemberResponse from(WeeklyBestMember best) {
        Week week = best.getWeek();
        Study study = week.getStudy();
        User user = best.getMember().getUser();
        return new BestMemberResponse(best.getId(), study.getId(), study.getName(), week.getId(),
                week.getWeekNo(), week.getTitle(), user.getId(), user.getName(), best.getReason(),
                best.getCreatedAt(), best.getUpdatedAt());
    }
}