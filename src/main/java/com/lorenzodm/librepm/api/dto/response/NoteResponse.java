package com.lorenzodm.librepm.api.dto.response;

import java.time.Instant;
import java.util.List;

public record NoteResponse(
        String id,
        String title,
        String content,
        String parentType,
        String parentId,
        String parentTitle,
        UserResponse owner,
        Instant createdAt,
        Instant updatedAt,
        List<TagResponse> tags,
        String noteType,
        boolean evidence,
        boolean frozen,
        Instant frozenAt,
        String visibility,
        String restrictedRoles
) {}
