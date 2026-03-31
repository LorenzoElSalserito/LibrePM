package com.lorenzodm.librepm.api.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record AddSuccessMetricRequest(
        @NotBlank @Size(max = 255) String name,
        @NotNull Double targetValue,
        Double currentValue,
        @Size(max = 50) String unit
) {}
