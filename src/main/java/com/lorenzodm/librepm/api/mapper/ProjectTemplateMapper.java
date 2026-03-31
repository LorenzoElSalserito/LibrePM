package com.lorenzodm.librepm.api.mapper;

import com.lorenzodm.librepm.api.dto.response.ProjectTemplateResponse;
import com.lorenzodm.librepm.core.entity.ProjectTemplate;
import org.springframework.stereotype.Component;

@Component
public class ProjectTemplateMapper {

    public ProjectTemplateResponse toResponse(ProjectTemplate template) {
        if (template == null) return null;
        return new ProjectTemplateResponse(
                template.getId(),
                template.getName(),
                template.getDescription(),
                template.getCategory(),
                template.getUseCases(),
                template.getPrerequisites(),
                template.getVersion(),
                template.isRequiresPlanningEngine(),
                template.getTemplateScope() != null ? template.getTemplateScope().name() : null,
                template.getStructureJson(),
                template.getPreviousVersionId(),
                template.getCapabilityProfile(),
                template.getComplexityLevel(),
                template.getCreatedAt(),
                template.getUpdatedAt()
        );
    }
}
