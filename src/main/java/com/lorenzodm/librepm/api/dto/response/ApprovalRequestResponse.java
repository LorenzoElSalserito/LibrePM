package com.lorenzodm.librepm.api.dto.response;

import java.time.Instant;

public record ApprovalRequestResponse(
        String id,
        String entityType,
        String entityId,
        String projectId,
        String requestedById,
        String requestedByName,
        String approverId,
        String approverName,
        String status,
        String comment,
        Instant requestedAt,
        Instant resolvedAt
) {}
