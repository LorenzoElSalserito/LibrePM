package com.lorenzodm.librepm.api.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateBaselineRequest(
        @NotBlank @Size(max = 255) String name
) {}
