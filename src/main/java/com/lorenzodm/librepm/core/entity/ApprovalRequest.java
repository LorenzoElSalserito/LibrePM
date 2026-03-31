package com.lorenzodm.librepm.core.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;

import java.time.Instant;

/**
 * Represents an approval request for budget revisions, grant submissions,
 * change requests, or other entities requiring sign-off.
 *
 * @author Lorenzo DM
 * @since 0.9.1
 */
@Entity
@Table(name = "approval_requests")
@SQLDelete(sql = "UPDATE approval_requests SET deleted_at = CURRENT_TIMESTAMP WHERE id = ?")
@Where(clause = "deleted_at IS NULL")
public class ApprovalRequest extends BaseSyncEntity {

    public enum Status {
        PENDING,
        APPROVED,
        REJECTED,
        WITHDRAWN
    }

    @Column(name = "entity_type", nullable = false, length = 64)
    private String entityType;

    @Column(name = "entity_id", nullable = false, length = 36)
    private String entityId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id")
    private Project project;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "requested_by", nullable = false)
    private User requestedBy;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "approver_id", nullable = false)
    private User approver;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private Status status = Status.PENDING;

    @Column(columnDefinition = "TEXT")
    private String comment;

    @Column(name = "requested_at")
    private Instant requestedAt = Instant.now();

    @Column(name = "resolved_at")
    private Instant resolvedAt;

    public ApprovalRequest() {
        super();
    }

    // Getters and Setters
    public String getEntityType() { return entityType; }
    public void setEntityType(String entityType) { this.entityType = entityType; }
    public String getEntityId() { return entityId; }
    public void setEntityId(String entityId) { this.entityId = entityId; }
    public Project getProject() { return project; }
    public void setProject(Project project) { this.project = project; }
    public User getRequestedBy() { return requestedBy; }
    public void setRequestedBy(User requestedBy) { this.requestedBy = requestedBy; }
    public User getApprover() { return approver; }
    public void setApprover(User approver) { this.approver = approver; }
    public Status getStatus() { return status; }
    public void setStatus(Status status) { this.status = status; }
    public String getComment() { return comment; }
    public void setComment(String comment) { this.comment = comment; }
    public Instant getRequestedAt() { return requestedAt; }
    public void setRequestedAt(Instant requestedAt) { this.requestedAt = requestedAt; }
    public Instant getResolvedAt() { return resolvedAt; }
    public void setResolvedAt(Instant resolvedAt) { this.resolvedAt = resolvedAt; }
}
