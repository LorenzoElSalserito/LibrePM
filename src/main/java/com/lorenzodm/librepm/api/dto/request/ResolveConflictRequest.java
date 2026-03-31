package com.lorenzodm.librepm.api.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

/**
 * Request to manually resolve a sync conflict (PRD-13-FR-005).
 * resolution must be one of: LOCAL, REMOTE, MANUAL, MERGE.
 */
public record ResolveConflictRequest(
        @NotBlank @Pattern(regexp = "LOCAL|REMOTE|MANUAL|MERGE") String resolution,
        /** Optional merged state JSON when resolution=MANUAL. */
        String mergedState
) {}
