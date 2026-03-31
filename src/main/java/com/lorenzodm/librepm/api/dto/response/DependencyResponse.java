package com.lorenzodm.librepm.api.dto.response;

import java.time.Instant;

public record DependencyResponse(
        String id,
        String predecessorId,
        String predecessorTitle,
        String successorId,
        String successorTitle,
        String type,
        Integer lag,
        Integer lead,
        Instant createdAt
) {}
