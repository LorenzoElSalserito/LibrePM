package com.lorenzodm.librepm.core.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * Represents a conflict detected during synchronization that requires manual resolution.
 *
 * @author Lorenzo DM
 * @since 1.0.0
 * @version 1.0.0
 */
@Entity
@Table(name = "conflict_records", indexes = {
    @Index(name = "idx_conflict_entity", columnList = "entity_type, entity_id")
})
public class ConflictRecord extends BaseSyncEntity {

    @Column(nullable = false, length = 100)
    private String entityType;

    @Column(nullable = false, length = 36)
    private String entityId;

    @Column(columnDefinition = "TEXT")
    private String localState;

    @Column(columnDefinition = "TEXT")
    private String remoteState;

    @Column(nullable = false)
    private LocalDateTime detectedAt;

    @Column(nullable = false)
    private boolean resolved;

    /** The specific field(s) involved in the conflict. NULL = whole entity. */
    @Column(length = 200)
    private String fieldName;

    /** The merge policy that was applied (or attempted). */
    @Column(length = 30)
    private String policyUsed;

    /**
     * Resolution chosen: LOCAL (keep local), REMOTE (accept remote),
     * MANUAL (human merged), MERGE (auto-merged).
     */
    @Column(length = 20)
    private String resolution;

    /** When the conflict was resolved. */
    @Column
    private LocalDateTime resolvedAt;

    /** ID of the user who resolved the conflict. */
    @Column(length = 36)
    private String resolvedBy;

    public ConflictRecord() {
        super();
    }

    // Getters and Setters
    public String getEntityType() { return entityType; }
    public void setEntityType(String entityType) { this.entityType = entityType; }
    public String getEntityId() { return entityId; }
    public void setEntityId(String entityId) { this.entityId = entityId; }
    public String getLocalState() { return localState; }
    public void setLocalState(String localState) { this.localState = localState; }
    public String getRemoteState() { return remoteState; }
    public void setRemoteState(String remoteState) { this.remoteState = remoteState; }
    public LocalDateTime getDetectedAt() { return detectedAt; }
    public void setDetectedAt(LocalDateTime detectedAt) { this.detectedAt = detectedAt; }
    public boolean isResolved() { return resolved; }
    public void setResolved(boolean resolved) { this.resolved = resolved; }
    public String getFieldName() { return fieldName; }
    public void setFieldName(String fieldName) { this.fieldName = fieldName; }
    public String getPolicyUsed() { return policyUsed; }
    public void setPolicyUsed(String policyUsed) { this.policyUsed = policyUsed; }
    public String getResolution() { return resolution; }
    public void setResolution(String resolution) { this.resolution = resolution; }
    public LocalDateTime getResolvedAt() { return resolvedAt; }
    public void setResolvedAt(LocalDateTime resolvedAt) { this.resolvedAt = resolvedAt; }
    public String getResolvedBy() { return resolvedBy; }
    public void setResolvedBy(String resolvedBy) { this.resolvedBy = resolvedBy; }
}
