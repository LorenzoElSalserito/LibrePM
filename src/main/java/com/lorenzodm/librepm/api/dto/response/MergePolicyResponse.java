package com.lorenzodm.librepm.api.dto.response;

import java.time.Instant;

public record MergePolicyResponse(
        String id,
        String entityType,
        String policy,
        String description,
        boolean autoResolvable,
        String fieldScope,
        Instant createdAt,
        Instant updatedAt
) {}
