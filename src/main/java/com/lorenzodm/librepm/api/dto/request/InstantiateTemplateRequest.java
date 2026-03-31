package com.lorenzodm.librepm.api.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Request to create a new project from a template (PRD-16-FR-004).
 * The created project is fully independent of the source template (PRD-16-BR-002).
 */
public record InstantiateTemplateRequest(
        @NotBlank @Size(max = 200) String projectName,
        @Size(max = 2000) String description,
        @Size(max = 7) String color,
        @Size(max = 50) String icon
) {}
