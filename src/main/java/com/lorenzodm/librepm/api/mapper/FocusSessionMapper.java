package com.lorenzodm.librepm.api.mapper;

import com.lorenzodm.librepm.api.dto.response.FocusSessionResponse;
import com.lorenzodm.librepm.core.entity.FocusSession;
import org.springframework.stereotype.Component;

@Component
public class FocusSessionMapper {

    public FocusSessionResponse toResponse(FocusSession fs) {
        return new FocusSessionResponse(
                fs.getId(),
                fs.getStartedAt(),
                fs.getEndedAt(),
                fs.getDurationMs(),
                fs.getNotes(),
                fs.getSessionType(),
                fs.getCreatedAt(),
                fs.getLastSyncedAt(),
                fs.getSyncStatus(),
                fs.getTask() != null ? fs.getTask().getId() : null,
                fs.getUser() != null ? fs.getUser().getId() : null,
                fs.isRunning()
        );
    }
}
