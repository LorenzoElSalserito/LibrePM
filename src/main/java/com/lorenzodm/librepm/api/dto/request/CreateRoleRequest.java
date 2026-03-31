package com.lorenzodm.librepm.api.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateRoleRequest(
        @NotBlank(message = "Nome ruolo obbligatorio")
        @Size(min = 1, max = 50)
        String name,
        @Size(max = 255)
        String description
) {}
