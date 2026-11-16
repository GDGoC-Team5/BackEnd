package com.gdgoc.studyhub.domain.bestmember.exception;

import com.gdgoc.studyhub.global.exception.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum BestMemberErrorCode implements ErrorCode {

    BEST_MEMBER_NOT_FOUND(HttpStatus.NOT_FOUND, "BEST_404", "선정된 우수 멤버가 없습니다."),
    TARGET_NOT_STUDY_MEMBER(HttpStatus.NOT_FOUND, "BEST_404_MEMBER", "우수 멤버로 선정할 스터디 멤버를 찾을 수 없습니다."),
    ALREADY_SELECTED(HttpStatus.CONFLICT, "BEST_409", "이미 우수 멤버가 선정된 주차입니다.");

    private final HttpStatus status;
    private final String code;
    private final String message;
}