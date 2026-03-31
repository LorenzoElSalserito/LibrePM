package com.lorenzodm.librepm.api.dto.response;

import java.time.Instant;

public record TaskStatusResponse(
        String id,
        String name,
        String description,
        String color,
        Instant createdAt
) {}
