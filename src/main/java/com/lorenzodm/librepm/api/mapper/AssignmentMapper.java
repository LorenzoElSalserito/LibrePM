package com.lorenzodm.librepm.api.mapper;

import com.lorenzodm.librepm.api.dto.response.AssignmentResponse;
import com.lorenzodm.librepm.core.entity.Assignment;
import org.springframework.stereotype.Component;

@Component
public class AssignmentMapper {

    public AssignmentResponse toResponse(Assignment assignment) {
        if (assignment == null) return null;
        return new AssignmentResponse(
                assignment.getId(),
                assignment.getTask().getId(),
                assignment.getTask().getTitle(),
                assignment.getUser().getId(),
                assignment.getUser().getDisplayName(),
                assignment.getRole() != null ? assignment.getRole().getId() : null,
                assignment.getRole() != null ? assignment.getRole().getName() : null,
                assignment.getAssignedAt(),
                assignment.getCreatedAt()
        );
    }
}
