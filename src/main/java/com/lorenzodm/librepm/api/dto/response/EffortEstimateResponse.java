package com.lorenzodm.librepm.api.dto.response;

import java.time.Instant;
import java.time.LocalDateTime;

public record EffortEstimateResponse(
        String id,
        String taskId,
        String taskTitle,
        String estimatorId,
        String estimatorDisplayName,
        int estimatedMinutes,
        LocalDateTime estimationDate,
        String rationale,
        Instant createdAt
) {}
