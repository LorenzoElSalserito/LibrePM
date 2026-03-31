package com.lorenzodm.librepm.api.dto.response;

import java.time.Instant;
import java.util.List;

public record SuccessMetricResponse(
        String id,
        String okrId,
        String name,
        double targetValue,
        double currentValue,
        String unit,
        double achievementPercentage,
        List<TargetAchievedRecordResponse> records,
        Instant createdAt,
        Instant updatedAt
) {}
