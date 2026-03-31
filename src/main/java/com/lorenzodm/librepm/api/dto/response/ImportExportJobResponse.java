package com.lorenzodm.librepm.api.dto.response;

import java.time.Instant;

public record ImportExportJobResponse(
        String id,
        String userId,
        String projectId,
        String jobType,
        String status,
        String resultPayload,
        String errorMessage,
        Integer recordCount,
        Instant completedAt,
        Instant createdAt
) {}
