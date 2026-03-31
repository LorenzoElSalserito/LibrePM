package com.lorenzodm.librepm.api.dto.response;

import java.time.Instant;

public record RemoteIdentityResponse(
        String id,
        String provider,
        String providerUserId,
        String email,
        Instant boundAt
) {}
