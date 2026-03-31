package com.lorenzodm.librepm.api.dto.response;

import java.time.Instant;

public record ProjectResponse(
        String id,
        String name,
        String description,
        String color,
        String icon,
        boolean archived,
        boolean favorite,
        Instant createdAt,
        Instant updatedAt,
        Instant lastSyncedAt,
        String syncStatus,
        String ownerId,
        int tasksCount,
        String visibility,
        String health,
        int overdueCount,
        String teamId,
        boolean timeTrackingEnabled,
        boolean planningEnabled,
        boolean financeEnabled,
        boolean grantsEnabled
) {}
