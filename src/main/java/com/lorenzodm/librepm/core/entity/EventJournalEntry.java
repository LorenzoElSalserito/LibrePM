package com.lorenzodm.librepm.core.entity;

import jakarta.persistence.*;

import java.time.Instant;

/**
 * Event journal entry for local observability and audit trail.
 * Uses auto-increment ID (not UUID) for efficient sequential storage.
 */
@Entity
@Table(name = "event_journal")
public class EventJournalEntry {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "event_type", nullable = false, length = 64)
    private String eventType;

    @Column(name = "entity_type", length = 64)
    private String entityType;

    @Column(name = "entity_id", length = 36)
    private String entityId;

    @Column(columnDefinition = "TEXT")
    private String payload;

    @Column(name = "user_id", length = 36)
    private String userId;

    @Column(nullable = false)
    private Instant timestamp;

    public EventJournalEntry() {
        this.timestamp = Instant.now();
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getEventType() { return eventType; }
    public void setEventType(String eventType) { this.eventType = eventType; }
    public String getEntityType() { return entityType; }
    public void setEntityType(String entityType) { this.entityType = entityType; }
    public String getEntityId() { return entityId; }
    public void setEntityId(String entityId) { this.entityId = entityId; }
    public String getPayload() { return payload; }
    public void setPayload(String payload) { this.payload = payload; }
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }
    public Instant getTimestamp() { return timestamp; }
    public void setTimestamp(Instant timestamp) { this.timestamp = timestamp; }
}
