package com.lorenzodm.librepm.api.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateWorkCalendarRequest(
        @NotBlank @Size(max = 100) String name,
        @Size(max = 255) String description
) {}
