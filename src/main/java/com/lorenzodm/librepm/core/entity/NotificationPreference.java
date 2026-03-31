package com.lorenzodm.librepm.core.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.UuidGenerator;

import java.time.Instant;

/**
 * Per-user notification preference for a specific event type and channel.
 *
 * @author Lorenzo DM
 * @since 0.9.1
 */
@Entity
@Table(name = "notification_preferences",
       uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "event_type", "channel"}))
public class NotificationPreference {

    public enum Channel {
        IN_APP,
        DESKTOP
    }

    public enum Severity {
        INFO,
        WARNING,
        CRITICAL
    }

    @Id
    @UuidGenerator
    private String id;

    @Column(name = "user_id", nullable = false, length = 36)
    private String userId;

    @Column(name = "event_type", nullable = false, length = 64)
    private String eventType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private Channel channel = Channel.IN_APP;

    @Column(nullable = false)
    private boolean enabled = true;

    @Enumerated(EnumType.STRING)
    @Column(name = "severity_threshold", length = 16)
    private Severity severityThreshold = Severity.INFO;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt = Instant.now();

    public NotificationPreference() {}

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }
    public String getEventType() { return eventType; }
    public void setEventType(String eventType) { this.eventType = eventType; }
    public Channel getChannel() { return channel; }
    public void setChannel(Channel channel) { this.channel = channel; }
    public boolean isEnabled() { return enabled; }
    public void setEnabled(boolean enabled) { this.enabled = enabled; }
    public Severity getSeverityThreshold() { return severityThreshold; }
    public void setSeverityThreshold(Severity severityThreshold) { this.severityThreshold = severityThreshold; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }

    @PreUpdate
    protected void onUpdate() { this.updatedAt = Instant.now(); }
}
