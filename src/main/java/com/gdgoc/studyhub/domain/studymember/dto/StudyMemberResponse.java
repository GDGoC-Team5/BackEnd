package com.gdgoc.studyhub.domain.studymember.dto;

import com.gdgoc.studyhub.domain.studymember.entity.StudyMember;
import com.gdgoc.studyhub.domain.studymember.entity.StudyRole;
import java.time.LocalDateTime;

/**
 * 스터디 멤버 목록의 한 줄. 이메일은 MANAGER 이거나 본인일 때만 채우고, 그 외에는 null 이다.
 * joinedAt 은 study_member 행이 처음 만들어진 시각이다(재가입해도 바뀌지 않음).
 */
public record StudyMemberResponse(Long userId, String name, String email, StudyRole role, LocalDateTime joinedAt) {

    public static StudyMemberResponse from(StudyMember member) {
        return of(member, false);
    }

    public static StudyMemberResponse of(StudyMember member, boolean includeEmail) {
        return new StudyMemberResponse(
                member.getUser().getId(),
                member.getUser().getName(),
                includeEmail ? member.getUser().getEmail() : null,
                member.getRole(),
                member.getCreatedAt());
    }
}
