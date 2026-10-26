package com.gdgoc.studyhub.domain.study.dto;

import com.gdgoc.studyhub.domain.study.entity.Study;
import com.gdgoc.studyhub.domain.studymember.entity.StudyRole;

/**
 * 스터디 상세. 멤버만 볼 수 있으므로 초대 코드도 함께 내려준다 (멤버 누구나 친구를 초대할 수 있다).
 */
public record StudyResponse(
        Long id,
        String name,
        String description,
        String inviteCode,
        StudyRole myRole,
        long memberCount
) {

    public static StudyResponse of(Study study, StudyRole myRole, long memberCount) {
        return new StudyResponse(study.getId(), study.getName(), study.getDescription(),
                study.getInviteCode(), myRole, memberCount);
    }
}
