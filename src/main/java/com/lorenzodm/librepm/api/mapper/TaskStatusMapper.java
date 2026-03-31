package com.lorenzodm.librepm.api.mapper;

import com.lorenzodm.librepm.api.dto.response.TaskStatusResponse;
import com.lorenzodm.librepm.core.entity.TaskStatus;
import org.springframework.stereotype.Component;

@Component
public class TaskStatusMapper {

    public TaskStatusResponse toResponse(TaskStatus status) {
        if (status == null) return null;
        return new TaskStatusResponse(
                status.getId(),
                status.getName(),
                status.getDescription(),
                status.getColor(),
                status.getCreatedAt()
        );
    }
}
