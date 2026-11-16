package com.gdgoc.studyhub.domain.submission.exception;

import com.gdgoc.studyhub.global.exception.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum SubmissionErrorCode implements ErrorCode {

    SUBMISSION_NOT_FOUND(HttpStatus.NOT_FOUND, "SUBMISSION_404", "제출물을 찾을 수 없습니다."),
    NOT_SUBMISSION_OWNER(HttpStatus.FORBIDDEN, "SUBMISSION_403_OWNER", "본인의 제출물만 변경할 수 있습니다."),
    NOT_ALLOWED_TO_READ(HttpStatus.FORBIDDEN, "SUBMISSION_403_READ", "본인 또는 운영진만 조회할 수 있습니다."),
    ALREADY_SUBMITTED(HttpStatus.CONFLICT, "SUBMISSION_409", "이미 제출한 주차입니다. 기존 제출물을 수정해 주세요."),
    UNSUPPORTED_ASSIGNMENT_TYPE(HttpStatus.BAD_REQUEST, "SUBMISSION_400_TYPE", "링크 제출 방식의 주차만 지원합니다.");

    private final HttpStatus status;
    private final String code;
    private final String message;
}
