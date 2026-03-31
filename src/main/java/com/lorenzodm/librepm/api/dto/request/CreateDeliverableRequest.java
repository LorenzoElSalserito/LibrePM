package com.lorenzodm.librepm.api.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record CreateDeliverableRequest(
        @NotBlank @Size(max = 255) String name,
        @Size(max = 1000) String description,
        LocalDate dueDate,
        @Min(0) @Max(100) Integer progress,
        String riskStatus // OK, AT_RISK, BLOCKED
) {}
