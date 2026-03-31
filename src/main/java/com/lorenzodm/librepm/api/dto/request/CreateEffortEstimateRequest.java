package com.lorenzodm.librepm.api.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateEffortEstimateRequest(
        @NotBlank(message = "ID task obbligatorio")
        String taskId,
        @Min(value = 1, message = "Stima deve essere almeno 1 minuto")
        int estimatedMinutes,
        @Size(max = 500)
        String rationale
) {}
