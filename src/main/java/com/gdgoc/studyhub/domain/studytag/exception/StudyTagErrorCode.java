package com.gdgoc.studyhub.domain.studytag.exception;

import com.gdgoc.studyhub.global.exception.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum StudyTagErrorCode implements ErrorCode {

    TAG_NOT_FOUND(HttpStatus.NOT_FOUND, "STUDY_404_TAG", "태그를 찾을 수 없습니다."),
    DUPLICATE_TAG(HttpStatus.CONFLICT, "STUDY_409_TAG", "이미 있는 태그입니다.");

    private final HttpStatus status;
    private final String code;
    private final String message;
}
