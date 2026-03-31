package com.lorenzodm.librepm.api.dto.response;

import java.time.Instant;

public record ProjectCharterResponse(
        String id,
        String projectId,
        String sponsor,
        String projectManager,
        String objectives,
        String problemStatement,
        String businessCase,
        Instant createdAt,
        Instant updatedAt
) {}
