package com.lorenzodm.librepm.api.dto.blueprint;

import java.util.List;

/**
 * DTO for a project phase (summary task) within a blueprint.
 */
public record TemplatePhaseDto(
        String internalId, // Optional ID for dependency mapping
        String name,
        List<TemplateTaskDto> tasks
) {}
