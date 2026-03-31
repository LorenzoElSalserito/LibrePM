package com.lorenzodm.librepm.api.dto.request;

import jakarta.validation.constraints.Size;

public record UpdateSuccessMetricRequest(
        @Size(max = 255) String name,
        Double targetValue,
        Double currentValue,
        @Size(max = 50) String unit
) {}
