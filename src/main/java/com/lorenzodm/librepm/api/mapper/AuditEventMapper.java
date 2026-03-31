package com.lorenzodm.librepm.api.mapper;

import com.lorenzodm.librepm.api.dto.response.AuditEventResponse;
import com.lorenzodm.librepm.core.entity.AuditEvent;
import org.springframework.stereotype.Component;

@Component
public class AuditEventMapper {

    public AuditEventResponse toResponse(AuditEvent event) {
        if (event == null) return null;
        return new AuditEventResponse(
                event.getId(),
                event.getEntityType(),
                event.getEntityId(),
                event.getAction() != null ? event.getAction().name() : null,
                event.getUserId(),
                event.getEventTimestamp(),
                event.getDetails()
        );
    }
}
