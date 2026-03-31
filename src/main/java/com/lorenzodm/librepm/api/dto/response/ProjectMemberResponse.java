package com.lorenzodm.librepm.api.dto.response;

import java.time.Instant;

public record ProjectMemberResponse(
        UserResponse user,
        String role,
        String systemRoleId,
        String systemRoleName,
        Instant joinedAt
) {}
