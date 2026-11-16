package com.gdgoc.studyhub.domain.study.dto;

import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * 스터디 수정 (PATCH). 보내지 않은(null) 값은 바꾸지 않는다.
 * name 은 보낸다면 공백만으로 채울 수 없다.
 */
public record StudyUpdateRequest(
        @Size(max = 50)
        @Pattern(regexp = ".*\\S.*", message = "스터디 이름은 비울 수 없습니다.")
        String name,

        @Size(max = 500)
        String description
) {
}
