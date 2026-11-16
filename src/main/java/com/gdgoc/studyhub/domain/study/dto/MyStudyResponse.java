package com.gdgoc.studyhub.domain.study.dto;

import com.gdgoc.studyhub.domain.studymember.entity.StudyMember;
import com.gdgoc.studyhub.domain.studymember.entity.StudyRole;

/** 내 스터디 목록의 한 줄. */
public record MyStudyResponse(Long id, String name, String description, StudyRole myRole) {

    public static MyStudyResponse from(StudyMember member) {
        return new MyStudyResponse(member.getStudy().getId(), member.getStudy().getName(),
                member.getStudy().getDescription(), member.getRole());
    }
}
