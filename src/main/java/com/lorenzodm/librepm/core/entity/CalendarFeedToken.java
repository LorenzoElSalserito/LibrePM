package com.lorenzodm.librepm.core.entity;

import jakarta.persistence.*;
import java.time.Instant;

/**
 * ICS calendar feed token for a user (PRD-14-FR-001).
 * One token per user; regenerable (PRD-14-BR-001).
 *
 * @author Lorenzo DM
 * @since 1.0.0
 */
@Entity
@Table(name = "calendar_feed_tokens", indexes = {
        @Index(name = "idx_cft_user", columnList = "user_id"),
        @Index(name = "idx_cft_token", columnList = "token")
})
public class CalendarFeedToken extends BaseSyncEntity {

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    /** Unique opaque token — used in the public ICS URL. */
    @Column(nullable = false, unique = true, length = 64)
    private String token;

    /**
     * Comma-separated entity types to include in the feed.
     * e.g. "Task,FocusSession" or "Task" (PRD-14-FR-002).
     */
    @Column(length = 200)
    private String includedEntityTypes = "Task";

    /** Optional human-readable label for the feed. */
    @Column(length = 100)
    private String description;

    /** When the feed was last subscribed / accessed. */
    @Column
    private Instant lastAccessedAt;

    public CalendarFeedToken() {
        super();
    }

    // Getters and Setters
    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }
    public String getToken() { return token; }
    public void setToken(String token) { this.token = token; }
    public String getIncludedEntityTypes() { return includedEntityTypes; }
    public void setIncludedEntityTypes(String includedEntityTypes) { this.includedEntityTypes = includedEntityTypes; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public Instant getLastAccessedAt() { return lastAccessedAt; }
    public void setLastAccessedAt(Instant lastAccessedAt) { this.lastAccessedAt = lastAccessedAt; }
}
