package com.gdgoc.studyhub.domain.studymember.dto;

import com.gdgoc.studyhub.domain.studymember.entity.StudyRole;
import jakarta.validation.constraints.NotNull;

public record StudyMemberRoleRequest(@NotNull StudyRole role) {}
