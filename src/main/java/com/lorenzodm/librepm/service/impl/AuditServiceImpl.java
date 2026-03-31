package com.lorenzodm.librepm.service.impl;

import com.lorenzodm.librepm.core.entity.AuditEvent;
import com.lorenzodm.librepm.repository.AuditEventRepository;
import com.lorenzodm.librepm.service.AuditService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

/**
 * Append-only audit log implementation (PRD-13-FR-006).
 */
@Service
@Transactional
public class AuditServiceImpl implements AuditService {

    private final AuditEventRepository auditEventRepository;

    public AuditServiceImpl(AuditEventRepository auditEventRepository) {
        this.auditEventRepository = auditEventRepository;
    }

    @Override
    public AuditEvent log(String entityType, String entityId,
                          AuditEvent.Action action, String userId, String detailsJson) {
        AuditEvent event = new AuditEvent();
        event.setEntityType(entityType);
        event.setEntityId(entityId);
        event.setAction(action);
        event.setUserId(userId);
        event.setDetails(detailsJson);
        event.setEventTimestamp(Instant.now());
        return auditEventRepository.save(event);
    }

    @Override
    @Transactional(readOnly = true)
    public List<AuditEvent> getByEntity(String entityType, String entityId) {
        return auditEventRepository.findByEntityTypeAndEntityIdOrderByEventTimestampDesc(entityType, entityId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<AuditEvent> getByUser(String userId) {
        return auditEventRepository.findByUserIdOrderByEventTimestampDesc(userId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<AuditEvent> getSince(Instant since) {
        return auditEventRepository.findByEventTimestampAfterOrderByEventTimestampDesc(since);
    }
}
