package com.lorenzodm.librepm.api.dto.request;

import jakarta.validation.constraints.Size;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Request per aggiornamento Task (v0.5.0)
 *
 * Nuovi campi:
 * - markdownNotes: Note in formato Markdown
 * - tagIds: Lista ID tag da associare
 * - reminderDate: Data/ora reminder
 * - reminderEnabled: Abilita/disabilita reminder
 * - estimatedMinutes: Stima tempo
 * - actualMinutes: Tempo effettivo
 * - type: Tipo task (MEETING, CALL, etc.)
 * - scheduledStart/End: Date schedulate
 *
 * @author Lorenzo DM
 * @since 0.2.0
 * @version 0.5.0
 */
public record UpdateTaskRequest(
        @Size(max = 500) String title,
        @Size(max = 5000) String description,
        String statusId,
        String priorityId,
        LocalDate deadline,
        @Size(max = 200) String owner,
        @Size(max = 2000) String notes,
        String markdownNotes,
        Boolean archived,
        Integer sortOrder,
        String assignedToId,

        // Reminder
        LocalDateTime reminderDate,
        Boolean reminderEnabled,

        // Time Tracking
        Integer estimatedEffort,
        Integer actualEffort,

        // Asset legacy
        @Size(max = 500) String assetPath,
        @Size(max = 100) String assetFileName,
        @Size(max = 50) String assetMimeType,
        Long assetSizeBytes,

        List<String> tagIds,

        // Task type & planning
        String type,
        LocalDateTime plannedStart,
        LocalDateTime plannedFinish,

        // Phase 2 - SUMMARY_TASK hierarchy
        String parentTaskId,

        // Extended fields (PRD-01-FR-006)
        String phaseId,
        String deliverableId,
        String stakeholderId,
        String fundingContext,
        Boolean planningEnabled,
        Boolean financeEnabled,
        LocalDate actualStart,
        LocalDate actualEnd,
        Double estimatedEffortHours,
        Double actualEffortHours
) {}
