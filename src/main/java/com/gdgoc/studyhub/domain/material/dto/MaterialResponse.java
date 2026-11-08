package com.gdgoc.studyhub.domain.material.dto;

import com.gdgoc.studyhub.domain.material.entity.Material;
import java.time.LocalDateTime;

public record MaterialResponse(Long id, Long weekId, Long memberId, Long userId, String name,
                               String title, String url, LocalDateTime createdAt, LocalDateTime updatedAt) {

    public static MaterialResponse from(Material material) {
        return new MaterialResponse(material.getId(), material.getWeek().getId(),
                material.getMember().getId(), material.getMember().getUser().getId(),
                material.getMember().getUser().getName(), material.getTitle(), material.getUrl(),
                material.getCreatedAt(), material.getUpdatedAt());
    }
}
