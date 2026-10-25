package com.gdgoc.studyhub.global.exception;

import org.springframework.http.HttpStatus;

/**
 * 모든 에러 코드의 공통 규격. 공통 에러는 {@link CommonErrorCode}, 도메인 에러는 각 도메인의
 * {@code domain/{도메인}/exception/{도메인}ErrorCode} enum 에 두고 이 인터페이스를 구현한다.
 * <pre>
 * &#64;Getter
 * &#64;RequiredArgsConstructor
 * public enum StudyErrorCode implements ErrorCode {
 *     STUDY_NOT_FOUND(HttpStatus.NOT_FOUND, "STUDY_404", "스터디를 찾을 수 없습니다.");
 *
 *     private final HttpStatus status;
 *     private final String code;
 *     private final String message;
 * }
 * </pre>
 */
public interface ErrorCode {

    HttpStatus getStatus();

    /** 응답의 error.code 값. 접두어로 도메인을 구분한다 (COMMON_, AUTH_, USER_, STUDY_ ...) */
    String getCode();

    String getMessage();
}
