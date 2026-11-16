package com.gdgoc.studyhub.domain.studytag.dto;

import com.gdgoc.studyhub.domain.studytag.entity.StudyTag;

public record StudyTagResponse(Long id, String name) {

    public static StudyTagResponse from(StudyTag tag) {
        return new StudyTagResponse(tag.getId(), tag.getName());
    }
}
