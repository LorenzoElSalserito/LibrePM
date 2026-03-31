package com.lorenzodm.librepm.api.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CreateRiskRegisterEntryRequest(
        @NotBlank @Size(max = 255) String description,
        @NotNull String probability, // LOW, MEDIUM, HIGH, CRITICAL
        @NotNull String impact,      // LOW, MEDIUM, HIGH, CRITICAL
        String mitigationStrategy
) {}
