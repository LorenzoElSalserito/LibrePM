package com.lorenzodm.librepm.service.impl;

import com.lorenzodm.librepm.api.dto.request.ResolveConflictRequest;
import com.lorenzodm.librepm.api.exception.ResourceNotFoundException;
import com.lorenzodm.librepm.core.entity.AuditEvent;
import com.lorenzodm.librepm.core.entity.ConflictRecord;
import com.lorenzodm.librepm.repository.ConflictRecordRepository;
import com.lorenzodm.librepm.service.AuditService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Handles manual conflict resolution (PRD-13-FR-005).
 * Resolution choices: LOCAL | REMOTE | MANUAL | MERGE.
 */
@Service
@Transactional
public class ConflictResolutionServiceImpl {

    private final ConflictRecordRepository conflictRecordRepository;
    private final AuditService auditService;

    public ConflictResolutionServiceImpl(ConflictRecordRepository conflictRecordRepository,
                                          AuditService auditService) {
        this.conflictRecordRepository = conflictRecordRepository;
        this.auditService = auditService;
    }

    public ConflictRecord resolve(String conflictId, ResolveConflictRequest req, String userId) {
        ConflictRecord conflict = conflictRecordRepository.findById(conflictId)
                .orElseThrow(() -> new ResourceNotFoundException("Conflitto non trovato: " + conflictId));

        if (conflict.isResolved()) {
            return conflict; // Idempotent: already resolved
        }

        conflict.setResolved(true);
        conflict.setResolution(req.resolution());
        conflict.setResolvedAt(LocalDateTime.now());
        conflict.setResolvedBy(userId);

        conflict = conflictRecordRepository.save(conflict);

        auditService.log(conflict.getEntityType(), conflict.getEntityId(),
                AuditEvent.Action.CONFLICT_RESOLVED, userId,
                "{\"conflictId\":\"" + conflictId + "\",\"resolution\":\"" + req.resolution() + "\"}");

        return conflict;
    }

    @Transactional(readOnly = true)
    public List<ConflictRecord> listUnresolved() {
        return conflictRecordRepository.findByResolvedFalseOrderByDetectedAtDesc();
    }

    @Transactional(readOnly = true)
    public List<ConflictRecord> listByEntity(String entityType, String entityId) {
        return conflictRecordRepository.findByEntityTypeAndEntityIdOrderByDetectedAtDesc(entityType, entityId);
    }

    @Transactional(readOnly = true)
    public ConflictRecord getById(String conflictId) {
        return conflictRecordRepository.findById(conflictId)
                .orElseThrow(() -> new ResourceNotFoundException("Conflitto non trovato: " + conflictId));
    }
}
