package com.lorenzodm.librepm.api.dto.request;

import jakarta.validation.constraints.Size;

import java.util.List;

public record UpdateNoteRequest(
        @Size(max = 200) String title,
        String content,
        List<String> tagIds,
        String noteType,
        Boolean evidence,
        Boolean frozen,
        String visibility,
        String restrictedRoles
) {}
