package com.gdgoc.studyhub.domain.week.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;

/** 보낸 필드만 수정한다. 제출 방식(assignmentType)은 수정할 수 없어서 받지 않는다. */
public record WeekUpdateRequest(
        @Min(value = 1, message = "주차 번호는 1 이상이어야 합니다.")
        Integer weekNo,

        @Size(min = 1, max = 100, message = "제목은 1자 이상 100자 이하여야 합니다.")
        String title,

        @Size(max = 2000, message = "과제 설명은 2000자 이하여야 합니다.")
        String assignmentDescription,

        LocalDateTime sessionAt,

        @Size(max = 100, message = "장소는 100자 이하여야 합니다.")
        String location,

        LocalDateTime deadline
) {
}