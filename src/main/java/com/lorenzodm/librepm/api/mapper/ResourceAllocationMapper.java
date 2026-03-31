package com.lorenzodm.librepm.api.mapper;

import com.lorenzodm.librepm.api.dto.response.ResourceAllocationItemResponse;
import com.lorenzodm.librepm.core.entity.ResourceAllocation;
import org.springframework.stereotype.Component;

@Component
public class ResourceAllocationMapper {

    public ResourceAllocationItemResponse toResponse(ResourceAllocation allocation) {
        if (allocation == null) return null;
        return new ResourceAllocationItemResponse(
                allocation.getId(),
                allocation.getUser().getId(),
                allocation.getUser().getDisplayName(),
                allocation.getProject() != null ? allocation.getProject().getId() : null,
                allocation.getProject() != null ? allocation.getProject().getName() : null,
                allocation.getStartDate(),
                allocation.getEndDate(),
                allocation.getPercentage(),
                allocation.getCreatedAt()
        );
    }
}
