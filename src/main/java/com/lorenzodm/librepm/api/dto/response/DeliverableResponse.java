package com.lorenzodm.librepm.api.dto.response;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;

public record DeliverableResponse(
        String id,
        String projectId,
        String name,
        String description,
        LocalDate dueDate,
        int progress,
        String riskStatus,
        LocalDateTime completedAt,
        Instant createdAt,
        Instant updatedAt
) {}
