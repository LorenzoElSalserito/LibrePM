package com.lorenzodm.librepm.service;

import com.lorenzodm.librepm.core.entity.ConflictRecord;
import com.lorenzodm.librepm.core.entity.MergePolicy;

import java.util.List;
import java.util.Optional;

/**
 * Core sync engine applying semantic merge policies (PRD-13).
 * PRD-13-FR-003: differentiated policies per entity type.
 * PRD-13-FR-004: LWW limited to appropriate cases.
 * PRD-13-FR-007: triggers plan recalculation after impactful merges.
 */
public interface SyncService {

    /**
     * Evaluates the merge policy for an entity type and creates a ConflictRecord
     * if resolution requires human review (PRD-13-FR-005).
     * Returns empty if the conflict was auto-resolved.
     */
    Optional<ConflictRecord> applyMerge(String entityType, String entityId,
                                         String localStateJson, String remoteStateJson,
                                         String userId);

    /**
     * Marks an entity as synced (syncStatus = SYNCED).
     */
    void markEntitySynced(String entityType, String entityId);

    /**
     * Returns the merge policy for an entity type.
     * Falls back to MANUAL if no policy is registered.
     */
    MergePolicy getPolicyFor(String entityType);

    /** Returns all unresolved conflict records (PRD-13-FR-005). */
    List<ConflictRecord> getPendingConflicts();

    /** Returns conflicts scoped to a specific entity. */
    List<ConflictRecord> getConflictsFor(String entityType, String entityId);

    /** Count of unresolved conflicts. */
    long countPendingConflicts();
}
