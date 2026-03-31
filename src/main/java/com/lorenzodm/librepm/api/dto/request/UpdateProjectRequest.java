package com.lorenzodm.librepm.api.dto.request;

import jakarta.validation.constraints.Size;

public record UpdateProjectRequest(
        @Size(max = 200) String name,
        @Size(max = 5000) String description,
        @Size(max = 20) String color,
        @Size(max = 50) String icon,
        Boolean archived,
        Boolean favorite,
        Boolean timeTrackingEnabled,
        Boolean planningEnabled,
        Boolean financeEnabled,
        Boolean grantsEnabled
) {}
