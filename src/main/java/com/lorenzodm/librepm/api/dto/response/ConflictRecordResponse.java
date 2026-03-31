package com.lorenzodm.librepm.api.dto.response;

import java.time.Instant;
import java.time.LocalDateTime;

public record ConflictRecordResponse(
        String id,
        String entityType,
        String entityId,
        String localState,
        String remoteState,
        LocalDateTime detectedAt,
        boolean resolved,
        String fieldName,
        String policyUsed,
        String resolution,
        LocalDateTime resolvedAt,
        String resolvedBy,
        Instant createdAt
) {}
