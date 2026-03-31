package com.lorenzodm.librepm.api.dto.request;

import jakarta.validation.constraints.Size;

/**
 * Configures which entity types to include in the ICS feed (PRD-14-FR-002).
 */
public record UpdateCalendarFeedRequest(
        /** Comma-separated entity types, e.g. "Task,FocusSession". */
        @Size(max = 200) String includedEntityTypes,
        @Size(max = 100) String description
) {}
