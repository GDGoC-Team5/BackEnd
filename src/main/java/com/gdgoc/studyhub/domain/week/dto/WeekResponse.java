package com.gdgoc.studyhub.domain.week.dto;

import com.gdgoc.studyhub.domain.week.entity.AssignmentType;
import com.gdgoc.studyhub.domain.week.entity.Week;
import java.time.LocalDateTime;

/** 주차 상세. 생성·수정 응답에도 같이 쓴다. */
public record WeekResponse(
        Long id,
        Long studyId,
        int weekNo,
        String title,
        String assignmentDescription,
        LocalDateTime sessionAt,
        String location,
        LocalDateTime deadline,
        AssignmentType assignmentType,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {

    public static WeekResponse from(Week week) {
        return new WeekResponse(week.getId(), week.getStudy().getId(), week.getWeekNo(), week.getTitle(),
                week.getAssignmentDescription(), week.getSessionAt(), week.getLocation(), week.getDeadline(),
                week.getAssignmentType(), week.getCreatedAt(), week.getUpdatedAt());
    }
}