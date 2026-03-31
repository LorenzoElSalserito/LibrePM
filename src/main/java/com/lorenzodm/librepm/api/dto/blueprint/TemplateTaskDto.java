package com.lorenzodm.librepm.api.dto.blueprint;

/**
 * DTO for a standard task within a blueprint's phase.
 */
public record TemplateTaskDto(
        String internalId, // Optional ID for dependency mapping
        String title,
        String description,
        String taskType, // e.g., "TASK", "MILESTONE"
        String priorityKey, // e.g., "HIGH", "MEDIUM"
        Integer estimatedEffort
) {}
