package com.gdgoc.studyhub.domain.studymember.dto;

import com.gdgoc.studyhub.domain.studymember.entity.StudyMember;
import com.gdgoc.studyhub.domain.studymember.entity.StudyRole;

/** 스터디 멤버 목록의 한 줄. 다른 멤버의 이메일은 노출하지 않는다. */
public record StudyMemberResponse(Long userId, String name, StudyRole role) {

    public static StudyMemberResponse from(StudyMember member) {
        return new StudyMemberResponse(member.getUser().getId(), member.getUser().getName(), member.getRole());
    }
}
