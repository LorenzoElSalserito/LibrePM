package com.lorenzodm.librepm.api.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;

public record UpdateUserRequest(
        @Email @Size(max = 100) String email,
        @Size(max = 100) String displayName,
        @Size(max = 255) String avatarPath,
        Boolean active
) {}
