package com.lorenzodm.librepm.api.dto.response;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Aggregated variance report comparing a baseline against the current plan (PRD-11).
 */
public record VarianceResponse(
        String baselineId,
        String baselineName,
        String projectId,

        // Schedule variance
        LocalDateTime baselineProjectEnd,
        LocalDateTime currentProjectEnd,
        long scheduleVarianceDays,        // positive = delayed

        // Effort variance
        int baselineTotalEffortMinutes,
        int currentTotalEffortMinutes,
        int effortVarianceMinutes,        // positive = more effort than planned

        // Task-level breakdown
        List<BaselineTaskSnapshotResponse> taskVariances,

        // PRD-11: overall project status
        String projectStatus              // ON_TRACK, AT_RISK, DELAYED
) {}
