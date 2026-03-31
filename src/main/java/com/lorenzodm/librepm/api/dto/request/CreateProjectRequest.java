package com.lorenzodm.librepm.api.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateProjectRequest(
        @NotBlank @Size(max = 200) String name,
        @Size(max = 5000) String description,
        @Size(max = 20) String color,
        @Size(max = 50) String icon,
        Boolean favorite,
        String visibility, // PERSONAL, TEAM, SHARED
        String teamId
) {}
