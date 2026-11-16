package com.gdgoc.studyhub.domain.week.exception;

import com.gdgoc.studyhub.global.exception.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum WeekErrorCode implements ErrorCode {

    WEEK_NOT_FOUND(HttpStatus.NOT_FOUND, "WEEK_404", "주차를 찾을 수 없습니다."),
    DUPLICATE_WEEK_NO(HttpStatus.CONFLICT, "WEEK_409_NO", "이미 존재하는 주차 번호입니다."),
    HAS_SUBMISSIONS(HttpStatus.CONFLICT, "WEEK_409_SUBMISSIONS", "제출물이 있는 주차는 삭제할 수 없습니다."),
    HAS_MATERIALS(HttpStatus.CONFLICT, "WEEK_409_MATERIALS", "세션 자료가 있는 주차는 삭제할 수 없습니다.");

    private final HttpStatus status;
    private final String code;
    private final String message;
}
