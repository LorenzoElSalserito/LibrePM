package com.lorenzodm.librepm.api.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

import java.time.LocalDate;

public record UpdateResourceAllocationRequest(
        LocalDate startDate,
        LocalDate endDate,
        @Min(1) @Max(100) Integer percentage
) {}
