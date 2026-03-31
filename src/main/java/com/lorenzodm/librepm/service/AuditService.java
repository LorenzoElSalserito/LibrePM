package com.lorenzodm.librepm.service;

import com.lorenzodm.librepm.core.entity.AuditEvent;

import java.time.Instant;
import java.util.List;

/**
 * Append-only audit log for critical changes (PRD-13-FR-006).
 * PRD-13-BR-005: metrics and dashboard data MUST preserve source and timestamp.
 */
public interface AuditService {

    /** Records a new audit event. Returns the persisted event. */
    AuditEvent log(String entityType, String entityId,
                   AuditEvent.Action action, String userId, String detailsJson);

    List<AuditEvent> getByEntity(String entityType, String entityId);

    List<AuditEvent> getByUser(String userId);

    List<AuditEvent> getSince(Instant since);
}
