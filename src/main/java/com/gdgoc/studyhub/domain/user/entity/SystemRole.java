package com.gdgoc.studyhub.domain.user.entity;

/**
 * 서비스 전체에서의 권한. 스터디별 권한(MANAGER/MEMBER)과는 별개다.
 * Spring Security 권한 문자열로 쓸 때는 "ROLE_" 접두어를 붙인다 (예: ROLE_USER).
 */
public enum SystemRole {
    USER,
    ADMIN
}
