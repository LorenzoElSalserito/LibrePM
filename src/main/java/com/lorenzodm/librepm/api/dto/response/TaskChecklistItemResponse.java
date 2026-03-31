package com.lorenzodm.librepm.api.dto.response;

import java.time.Instant;

public record TaskChecklistItemResponse(
        String id,
        String text,
        boolean done,
        int sortOrder,
        Instant createdAt,
        Instant updatedAt
) {}
