package com.lorenzodm.librepm.api.dto.request;

import jakarta.validation.constraints.Size;

public record UpdateRiskRegisterEntryRequest(
        @Size(max = 255) String description,
        String probability, // LOW, MEDIUM, HIGH, CRITICAL
        String impact,      // LOW, MEDIUM, HIGH, CRITICAL
        String mitigationStrategy
) {}
