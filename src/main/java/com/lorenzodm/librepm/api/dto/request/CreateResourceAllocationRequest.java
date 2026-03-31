package com.lorenzodm.librepm.api.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public record CreateResourceAllocationRequest(
        @NotBlank String userId,
        String projectId,
        @NotNull LocalDate startDate,
        @NotNull LocalDate endDate,
        @Min(1) @Max(100) int percentage
) {}
