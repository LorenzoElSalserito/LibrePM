package com.lorenzodm.librepm.api.dto.response;

import java.time.Instant;

public record NotificationResponse(
    String id,
    String message,
    String type,
    String referenceType,
    String referenceId,
    boolean read,
    Instant createdAt,
    UserResponse sender
) {}
