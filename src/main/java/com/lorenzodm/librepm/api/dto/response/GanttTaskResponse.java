package com.lorenzodm.librepm.api.dto.response;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Response DTO for Gantt/Timeline view.
 * Contains all data needed to render a task bar on the Gantt chart.
 *
 * @author Lorenzo DM
 * @since 1.0.0
 * @version 0.6.1
 */
public record GanttTaskResponse(
        String id,
        String title,
        String type,         // TASK, SUMMARY_TASK, MILESTONE
        String statusName,
        String statusColor,
        String priorityName,
        int priorityLevel,
        LocalDateTime plannedStart,
        LocalDateTime plannedFinish,
        LocalDate deadline,
        Instant createdAt,
        Integer estimatedEffort,
        Integer actualEffort,
        int progress,        // 0-100, computed from checklist or effort
        String parentTaskId,
        String wbsCode,
        int sortOrder,
        boolean isBlocked,
        boolean isCritical,  // On critical path
        List<DependencyRef> dependencies
) {
    public record DependencyRef(
            String dependencyId,
            String predecessorId,
            String type,
            Integer lag,
            Integer lead
    ) {}
}
