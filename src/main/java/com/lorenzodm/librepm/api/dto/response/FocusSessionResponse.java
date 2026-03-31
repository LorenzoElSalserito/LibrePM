package com.lorenzodm.librepm.api.dto.response;

import java.time.Instant;

public record FocusSessionResponse(
        String id,
        Instant startedAt,
        Instant endedAt,
        long durationMs,
        String notes,
        String sessionType,
        Instant createdAt,
        Instant lastSyncedAt,
        String syncStatus,
        String taskId,
        String userId,
        boolean running
) {}
