package com.gdgoc.studyhub.domain.user.dto;

import com.gdgoc.studyhub.domain.user.entity.SystemRole;
import com.gdgoc.studyhub.domain.user.entity.User;

/**
 * 회원 정보 응답. 엔티티를 그대로 반환하면 password(해시)까지 직렬화되므로 반드시 이 DTO 로 변환한다.
 */
public record UserResponse(Long id, String email, String name, SystemRole systemRole) {

    public static UserResponse from(User user) {
        return new UserResponse(user.getId(), user.getEmail(), user.getName(), user.getSystemRole());
    }
}
