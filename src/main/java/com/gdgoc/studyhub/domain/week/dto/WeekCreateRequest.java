package com.gdgoc.studyhub.domain.week.dto;

import com.gdgoc.studyhub.domain.week.entity.AssignmentType;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;

public record WeekCreateRequest(
        @NotNull(message = "주차 번호는 필수입니다.")
        @Min(value = 1, message = "주차 번호는 1 이상이어야 합니다.")
        Integer weekNo,

        @NotBlank(message = "제목은 필수입니다.")
        @Size(max = 100, message = "제목은 100자 이하여야 합니다.")
        String title,

        @Size(max = 2000, message = "과제 설명은 2000자 이하여야 합니다.")
        String assignmentDescription,

        LocalDateTime sessionAt,

        @Size(max = 100, message = "장소는 100자 이하여야 합니다.")
        String location,

        @NotNull(message = "마감 일시는 필수입니다.")
        LocalDateTime deadline,

        @NotNull(message = "제출 방식은 필수입니다.")
        AssignmentType assignmentType
) {
}