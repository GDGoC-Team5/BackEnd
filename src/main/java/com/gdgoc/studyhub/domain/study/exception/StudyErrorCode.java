package com.gdgoc.studyhub.domain.study.exception;

import com.gdgoc.studyhub.global.exception.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

/**
 * 스터디 자체에 대한 에러 코드. 멤버·권한 에러는 StudyMemberErrorCode, 태그 에러는 StudyTagErrorCode 에 있다.
 */
@Getter
@RequiredArgsConstructor
public enum StudyErrorCode implements ErrorCode {

    STUDY_NOT_FOUND(HttpStatus.NOT_FOUND, "STUDY_404", "스터디를 찾을 수 없습니다.");

    private final HttpStatus status;
    private final String code;
    private final String message;
}
