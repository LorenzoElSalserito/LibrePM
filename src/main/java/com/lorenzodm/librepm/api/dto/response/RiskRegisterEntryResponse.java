package com.lorenzodm.librepm.api.dto.response;

import java.time.Instant;

public record RiskRegisterEntryResponse(
        String id,
        String projectId,
        String description,
        String probability,
        String impact,
        String mitigationStrategy,
        Instant createdAt,
        Instant updatedAt
) {}
