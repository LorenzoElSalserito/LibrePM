package com.lorenzodm.librepm.api.dto.response;

import java.time.Instant;

public record ExternalContributorResponse(
        String id,
        String displayName,
        String email,
        String organization,
        String roleId,
        String roleName,
        String scope,
        String scopeEntityId,
        Instant accessExpiresAt,
        Instant createdAt,
        String createdById
) {}
