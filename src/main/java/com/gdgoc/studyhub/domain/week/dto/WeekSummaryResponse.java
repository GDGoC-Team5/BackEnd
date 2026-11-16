package com.gdgoc.studyhub.domain.week.dto;

import com.gdgoc.studyhub.domain.week.entity.AssignmentType;
import com.gdgoc.studyhub.domain.week.entity.Week;
import java.time.LocalDateTime;

/** 주차 목록의 한 줄. */
public record WeekSummaryResponse(
        Long id,
        int weekNo,
        String title,
        String assignmentDescription,
        LocalDateTime sessionAt,
        String location,
        LocalDateTime deadline,
        AssignmentType assignmentType,
        LocalDateTime createdAt
) {

    public static WeekSummaryResponse from(Week week) {
        return new WeekSummaryResponse(week.getId(), week.getWeekNo(), week.getTitle(),
                week.getAssignmentDescription(), week.getSessionAt(),
                week.getLocation(), week.getDeadline(), week.getAssignmentType(), week.getCreatedAt());
    }
}
