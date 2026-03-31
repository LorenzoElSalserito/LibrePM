package com.lorenzodm.librepm.api.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateProjectTemplateRequest(
        @NotBlank @Size(max = 100) String name,
        @Size(max = 500) String description,
        @Size(max = 50) String category,
        String useCases,
        String prerequisites,
        boolean requiresPlanningEngine,
        /** JSON blueprint. If null an empty blueprint is used. */
        String structureJson
) {}
