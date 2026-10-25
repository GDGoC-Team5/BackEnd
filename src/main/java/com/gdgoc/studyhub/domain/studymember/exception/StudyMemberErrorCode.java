package com.gdgoc.studyhub.domain.studymember.exception;

import com.gdgoc.studyhub.global.exception.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum StudyMemberErrorCode implements ErrorCode {

    // 권한 (스터디 권한 체크 모듈)
    NOT_STUDY_MEMBER(HttpStatus.FORBIDDEN, "STUDY_403_MEMBER", "스터디 멤버만 접근할 수 있습니다."),
    NOT_STUDY_MANAGER(HttpStatus.FORBIDDEN, "STUDY_403_MANAGER", "스터디 매니저만 할 수 있습니다."),

    // 가입 · 탈퇴
    INVALID_INVITE_CODE(HttpStatus.NOT_FOUND, "STUDY_404_INVITE", "유효하지 않은 초대 코드입니다."),
    ALREADY_STUDY_MEMBER(HttpStatus.CONFLICT, "STUDY_409_MEMBER", "이미 가입한 스터디입니다."),
    LAST_MANAGER_CANNOT_LEAVE(HttpStatus.CONFLICT, "STUDY_409_LAST_MANAGER",
            "마지막 매니저는 다른 멤버가 남아 있는 동안 탈퇴할 수 없습니다.");

    private final HttpStatus status;
    private final String code;
    private final String message;
}
