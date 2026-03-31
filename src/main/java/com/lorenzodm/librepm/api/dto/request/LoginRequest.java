package com.lorenzodm.librepm.api.dto.request;

import jakarta.validation.constraints.NotBlank;

/**
 * DTO Request per login
 *
 * @author Lorenzo DM
 * @since 0.5.3
 */
public record LoginRequest(
        @NotBlank(message = "User ID obbligatorio")
        String userId,

        @NotBlank(message = "Password obbligatoria")
        String password
) {}