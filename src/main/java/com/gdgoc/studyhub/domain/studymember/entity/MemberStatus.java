package com.gdgoc.studyhub.domain.studymember.entity;

/**
 * 스터디 멤버 상태. 탈퇴해도 행을 지우지 않고 LEFT 로 바꿔서 제출 기록 등과의 연결을 유지한다.
 */
public enum MemberStatus {
    ACTIVE,
    LEFT
}
