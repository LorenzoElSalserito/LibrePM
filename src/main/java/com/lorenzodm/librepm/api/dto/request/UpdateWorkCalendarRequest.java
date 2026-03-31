package com.lorenzodm.librepm.api.dto.request;

import jakarta.validation.constraints.Size;

public record UpdateWorkCalendarRequest(
        @Size(max = 100) String name,
        @Size(max = 255) String description
) {}
