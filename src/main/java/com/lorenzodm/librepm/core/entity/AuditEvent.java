package com.lorenzodm.librepm.core.entity;

import jakarta.persistence.*;
import java.time.Instant;

/**
 * Append-only audit log for critical planning and dashboard changes (PRD-13-FR-006).
 * Immutable once created; no update or delete via API.
 *
 * @author Lorenzo DM
 * @since 1.0.0
 */
@Entity
@Table(name = "audit_events", indexes = {
        @Index(name = "idx_audit_entity", columnList = "entity_type, entity_id"),
        @Index(name = "idx_audit_user", columnList = "user_id"),
        @Index(name = "idx_audit_ts", columnList = "event_timestamp")
})
public class AuditEvent extends BaseSyncEntity {

    public enum Action {
        CREATED, UPDATED, DELETED,
        STATUS_CHANGED, ARCHIVED, RESTORED,
        MERGED, SYNCED, CONFLICT_DETECTED, CONFLICT_RESOLVED,
        BASELINE_CREATED, PLAN_RECALCULATED,
        IMPORTED, EXPORTED,
        LOGIN, PASSWORD_MIGRATED,
        APPROVAL_REQUESTED, APPROVAL_GRANTED, APPROVAL_REJECTED
    }

    @Column(nullable = false, length = 100)
    private String entityType;

    @Column(nullable = false, length = 36)
    private String entityId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private Action action;

    /** The user who triggered the change. Null for system-triggered events. */
    @Column(length = 36)
    private String userId;

    @Column(name = "event_timestamp", nullable = false)
    private Instant eventTimestamp;

    /** JSON payload with relevant context (field changes, merge details, etc.). */
    @Column(columnDefinition = "TEXT")
    private String details;

    public AuditEvent() {
        super();
        this.eventTimestamp = Instant.now();
    }

    // Getters and Setters
    public String getEntityType() { return entityType; }
    public void setEntityType(String entityType) { this.entityType = entityType; }
    public String getEntityId() { return entityId; }
    public void setEntityId(String entityId) { this.entityId = entityId; }
    public Action getAction() { return action; }
    public void setAction(Action action) { this.action = action; }
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }
    public Instant getEventTimestamp() { return eventTimestamp; }
    public void setEventTimestamp(Instant eventTimestamp) { this.eventTimestamp = eventTimestamp; }
    public String getDetails() { return details; }
    public void setDetails(String details) { this.details = details; }
}
