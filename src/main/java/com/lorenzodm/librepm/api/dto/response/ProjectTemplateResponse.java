package com.lorenzodm.librepm.api.dto.response;

import java.time.Instant;

public record ProjectTemplateResponse(
        String id,
        String name,
        String description,
        String category,
        String useCases,
        String prerequisites,
        String version,
        boolean requiresPlanningEngine,
        String templateScope,   // SYSTEM | WORKSPACE | USER
        String structureJson,
        String previousVersionId,
        String capabilityProfile,
        String complexityLevel,
        Instant createdAt,
        Instant updatedAt
) {}
