package com.lorenzodm.librepm.api.dto.response;

import java.time.LocalDateTime;

public record BaselineTaskSnapshotResponse(
        String id,
        String taskId,
        String taskTitle,
        LocalDateTime plannedStart,
        LocalDateTime plannedFinish,
        Integer estimatedEffort,

        // Current vs Baseline variance (populated by VarianceService)
        LocalDateTime currentPlannedStart,
        LocalDateTime currentPlannedFinish,
        Integer currentEstimatedEffort,
        Long scheduleVarianceMinutes,  // positive = delayed, negative = ahead
        Integer effortVarianceMinutes   // positive = over-estimated, negative = under-estimated
) {}
