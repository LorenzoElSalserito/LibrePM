package com.lorenzodm.librepm.api.dto.request;

import jakarta.validation.constraints.NotBlank;

public record AddMemberRequest(
        @NotBlank String userId,
        @NotBlank String role // OWNER, EDITOR, VIEWER
) {}
