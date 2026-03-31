package com.lorenzodm.librepm.api.mapper;

import com.lorenzodm.librepm.api.dto.response.ProjectCharterResponse;
import com.lorenzodm.librepm.core.entity.ProjectCharter;
import org.springframework.stereotype.Component;

@Component
public class ProjectCharterMapper {

    public ProjectCharterResponse toResponse(ProjectCharter charter) {
        if (charter == null) return null;
        return new ProjectCharterResponse(
                charter.getId(),
                charter.getProject().getId(),
                charter.getSponsor(),
                charter.getProjectManager(),
                charter.getObjectives(),
                charter.getProblemStatement(),
                charter.getBusinessCase(),
                charter.getCreatedAt(),
                charter.getUpdatedAt()
        );
    }
}
