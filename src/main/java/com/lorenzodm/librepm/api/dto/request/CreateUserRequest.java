package com.lorenzodm.librepm.api.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateUserRequest(
        @NotBlank @Size(min = 3, max = 50) String username,
        @Email @Size(max = 100) String email,
        @NotBlank @Size(min = 6, max = 200) String password,
        @Size(max = 100) String displayName
) {}
