package com.lorenzodm.librepm.api.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateExternalContributorRequest(
        @NotBlank @Size(max = 128) String displayName,
        @Size(max = 255) String email,
        @Size(max = 255) String organization,
        String roleId,
        String scope,
        String scopeEntityId
) {}
