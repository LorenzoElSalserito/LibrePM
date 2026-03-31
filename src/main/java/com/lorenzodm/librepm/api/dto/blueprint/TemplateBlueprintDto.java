package com.lorenzodm.librepm.api.dto.blueprint;

import java.util.List;

/**
 * Represents the structure of a Project Template serialized as JSON.
 * <p>
 * This DTO is used by {@link com.lorenzodm.librepm.service.impl.TemplateInstantiationServiceImpl}
 * to parse and create the project structure.
 * </p>
 */
public record TemplateBlueprintDto(
    String name,
    String description,
    List<TemplatePhaseDto> phases,
    List<TemplateDeliverableDto> deliverables,
    List<TemplateOkrDto> okrs,
    TemplateCharterDto charter,
    List<TemplateDependencyDto> dependencies
) {}
