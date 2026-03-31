package com.lorenzodm.librepm.api.dto.response;

import java.time.Instant;

public record UserConnectionResponse(
        String id,
        UserResponse user, // Requester or Target based on context
        String status,
        Instant createdAt
) {}
