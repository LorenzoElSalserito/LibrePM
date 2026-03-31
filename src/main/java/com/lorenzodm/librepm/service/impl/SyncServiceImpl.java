package com.lorenzodm.librepm.service.impl;

import com.lorenzodm.librepm.core.entity.AuditEvent;
import com.lorenzodm.librepm.core.entity.ConflictRecord;
import com.lorenzodm.librepm.core.entity.MergePolicy;
import com.lorenzodm.librepm.repository.ConflictRecordRepository;
import com.lorenzodm.librepm.repository.MergePolicyRepository;
import com.lorenzodm.librepm.service.AuditService;
import com.lorenzodm.librepm.service.SyncService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * PRD-13: Semantic sync engine.
 * Applies entity-specific merge policies and records unresolvable conflicts.
 * PRD-13-FR-003: differentiated strategies per entity type.
 * PRD-13-FR-004: LWW only where semantically safe.
 * PRD-13-FR-007: plan recalculation signalled via AuditEvent.
 */
@Service
@Transactional
public class SyncServiceImpl implements SyncService {

    private static final MergePolicy FALLBACK_MANUAL;

    static {
        FALLBACK_MANUAL = new MergePolicy();
        FALLBACK_MANUAL.setEntityType("*");
        FALLBACK_MANUAL.setPolicy(MergePolicy.PolicyType.MANUAL);
        FALLBACK_MANUAL.setAutoResolvable(false);
        FALLBACK_MANUAL.setDescription("Default fallback: require manual review");
    }

    private final MergePolicyRepository mergePolicyRepository;
    private final ConflictRecordRepository conflictRecordRepository;
    private final AuditService auditService;

    public SyncServiceImpl(MergePolicyRepository mergePolicyRepository,
                           ConflictRecordRepository conflictRecordRepository,
                           AuditService auditService) {
        this.mergePolicyRepository = mergePolicyRepository;
        this.conflictRecordRepository = conflictRecordRepository;
        this.auditService = auditService;
    }

    @Override
    public Optional<ConflictRecord> applyMerge(String entityType, String entityId,
                                                String localStateJson, String remoteStateJson,
                                                String userId) {
        MergePolicy policy = getPolicyFor(entityType);

        // Log sync event
        auditService.log(entityType, entityId, AuditEvent.Action.SYNCED, userId,
                "{\"policy\":\"" + policy.getPolicy().name() + "\"}");

        if (policy.isAutoResolvable()) {
            // Auto-resolution: log MERGED and return empty (no conflict record needed)
            auditService.log(entityType, entityId, AuditEvent.Action.MERGED, userId,
                    "{\"policy\":\"" + policy.getPolicy().name() + "\",\"autoResolved\":true}");
            return Optional.empty();
        }

        // PRD-13-FR-005: create ConflictRecord for manual review
        ConflictRecord conflict = new ConflictRecord();
        conflict.setEntityType(entityType);
        conflict.setEntityId(entityId);
        conflict.setLocalState(localStateJson);
        conflict.setRemoteState(remoteStateJson);
        conflict.setDetectedAt(LocalDateTime.now());
        conflict.setResolved(false);
        conflict.setPolicyUsed(policy.getPolicy().name());

        conflict = conflictRecordRepository.save(conflict);

        auditService.log(entityType, entityId, AuditEvent.Action.CONFLICT_DETECTED, userId,
                "{\"conflictId\":\"" + conflict.getId() + "\",\"policy\":\"" + policy.getPolicy().name() + "\"}");

        // PRD-13-FR-007: if this entity type affects the plan graph, signal recalculation
        if (isPlanAffecting(entityType)) {
            auditService.log(entityType, entityId, AuditEvent.Action.PLAN_RECALCULATED, userId,
                    "{\"trigger\":\"conflict_detected\",\"conflictId\":\"" + conflict.getId() + "\"}");
        }

        return Optional.of(conflict);
    }

    @Override
    public void markEntitySynced(String entityType, String entityId) {
        // syncStatus is on BaseSyncEntity; the actual update happens in the entity's save cycle.
        // Here we log the SYNCED event for audit trail.
        auditService.log(entityType, entityId, AuditEvent.Action.SYNCED, null, null);
    }

    @Override
    @Transactional(readOnly = true)
    public MergePolicy getPolicyFor(String entityType) {
        return mergePolicyRepository.findByEntityType(entityType)
                .orElse(FALLBACK_MANUAL);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ConflictRecord> getPendingConflicts() {
        return conflictRecordRepository.findByResolvedFalseOrderByDetectedAtDesc();
    }

    @Override
    @Transactional(readOnly = true)
    public List<ConflictRecord> getConflictsFor(String entityType, String entityId) {
        return conflictRecordRepository.findByEntityTypeAndEntityIdOrderByDetectedAtDesc(entityType, entityId);
    }

    @Override
    @Transactional(readOnly = true)
    public long countPendingConflicts() {
        return conflictRecordRepository.countByResolvedFalse();
    }

    // --- Private helpers ---

    /** Entity types whose conflict resolution requires plan recalculation (PRD-13-FR-007). */
    private boolean isPlanAffecting(String entityType) {
        return switch (entityType) {
            case "Dependency", "Task", "WbsNode", "WorkCalendar", "ResourceAllocation" -> true;
            default -> false;
        };
    }
}
