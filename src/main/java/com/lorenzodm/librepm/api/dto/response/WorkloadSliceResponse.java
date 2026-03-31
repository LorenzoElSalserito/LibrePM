package com.lorenzodm.librepm.api.dto.response;

import java.time.LocalDate;
import java.util.List;

/**
 * Workload view data for a user over a period (PRD-12).
 * Shows capacity vs actual workload per day/week.
 */
public record WorkloadSliceResponse(
        String userId,
        String userDisplayName,
        LocalDate periodStart,
        LocalDate periodEnd,
        int availableMinutes,      // from calendar
        int allocatedMinutes,      // from resource allocations (percentage * working minutes)
        int assignedEffortMinutes, // from task assignments in this period
        boolean isOverbooked,      // PRD-12-FR-007
        List<TaskSlice> tasks
) {
    public record TaskSlice(
            String taskId,
            String taskTitle,
            String statusName,
            Integer estimatedEffort,
            Integer actualEffort
    ) {}
}
