package com.lorenzodm.librepm.api.dto.response;

import java.time.Instant;
import java.time.LocalDate;

public record TimeEntryResponse(
        String id,
        String taskId,
        String taskTitle,
        String userId,
        String userDisplayName,
        LocalDate entryDate,
        int durationMinutes,
        String type,
        String description,
        Instant createdAt
) {}
