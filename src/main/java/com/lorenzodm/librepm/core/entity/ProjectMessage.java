package com.lorenzodm.librepm.core.entity;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "project_messages")
public class ProjectMessage {
    @Id @Column(length = 36)
    private String id = UUID.randomUUID().toString();
    @Column(name = "project_id", nullable = false, length = 36)
    private String projectId;
    @Column(name = "sender_id", nullable = false, length = 36)
    private String senderId;
    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;
    protected ProjectMessage() {}
    public ProjectMessage(String projectId, String senderId, String content) {
        this.projectId = projectId;
        this.senderId = senderId;
        this.content = content;
    }
    public String getId() { return id; }
    public String getProjectId() { return projectId; }
    public String getSenderId() { return senderId; }
    public String getContent() { return content; }
    public Instant getCreatedAt() { return createdAt; }

    // Hibernate's @CreationTimestamp/@UpdateTimestamp truncate to whole seconds on
    // converter-mapped columns: same-second notes and messages lost their order.
    @PrePersist
    protected void initAuditTimestamps() {
        Instant now = Instant.now();
        createdAt = now;
    }
}
