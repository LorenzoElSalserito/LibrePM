package com.lorenzodm.librepm.api.dto.response;

import java.time.Instant;
import java.time.LocalDateTime;

public record AssignmentResponse(
        String id,
        String taskId,
        String taskTitle,
        String userId,
        String userDisplayName,
        String roleId,
        String roleName,
        LocalDateTime assignedAt,
        Instant createdAt
) {}
