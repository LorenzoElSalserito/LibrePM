package com.lorenzodm.librepm.api.dto.response;

import java.time.Instant;
import java.util.List;

public record RoleResponse(
        String id,
        String name,
        String description,
        List<String> permissions,
        Instant createdAt
) {}
