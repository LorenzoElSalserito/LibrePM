package com.lorenzodm.librepm.api.dto.blueprint;

import com.lorenzodm.librepm.core.entity.Dependency;

/**
 * DTO for defining a dependency between two tasks within a blueprint.
 * Used during template instantiation to reconstruct the dependency graph.
 */
public record TemplateDependencyDto(
        String fromTaskInternalId, // Internal ID within the blueprint JSON
        String toTaskInternalId,   // Internal ID within the blueprint JSON
        Dependency.DependencyType type,      // Corrected from Dependency.Type
        Integer lagMinutes         // Lag/Lead time
) {}
