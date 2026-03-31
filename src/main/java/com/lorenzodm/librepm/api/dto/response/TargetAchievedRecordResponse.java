package com.lorenzodm.librepm.api.dto.response;

import java.time.Instant;
import java.time.LocalDateTime;

public record TargetAchievedRecordResponse(
        String id,
        String metricId,
        double achievedValue,
        LocalDateTime recordDate,
        String note,
        Instant createdAt
) {}
