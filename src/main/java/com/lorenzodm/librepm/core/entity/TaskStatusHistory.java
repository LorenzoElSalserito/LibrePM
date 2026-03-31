package com.lorenzodm.librepm.core.entity;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

/**
 * Records every status change of a task (PRD-01-FR-008).
 * <p>
 * Provides an essential, immutable history of status transitions
 * including completion, reopening, and archival events.
 * </p>
 *
 * @author Lorenzo DM
 * @since 0.10.0
 */
@Entity
@Table(name = "task_status_history", indexes = {
        @Index(name = "idx_task_status_history", columnList = "task_id, changed_at"),
        @Index(name = "idx_task_status_history_user", columnList = "changed_by")
})
public class TaskStatusHistory {

    @Id
    @Column(length = 36)
    private String id;

    @Column(name = "task_id", nullable = false, length = 36)
    private String taskId;

    @Column(name = "old_status", length = 64)
    private String oldStatus;

    @Column(name = "new_status", nullable = false, length = 64)
    private String newStatus;

    @Column(name = "changed_by", length = 36)
    private String changedBy;

    @Column(name = "changed_at", nullable = false)
    private Instant changedAt;

    @Column(columnDefinition = "TEXT")
    private String comment;

    public TaskStatusHistory() {
        this.id = UUID.randomUUID().toString();
        this.changedAt = Instant.now();
    }

    public TaskStatusHistory(String taskId, String oldStatus, String newStatus, String changedBy) {
        this();
        this.taskId = taskId;
        this.oldStatus = oldStatus;
        this.newStatus = newStatus;
        this.changedBy = changedBy;
    }

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getTaskId() { return taskId; }
    public void setTaskId(String taskId) { this.taskId = taskId; }
    public String getOldStatus() { return oldStatus; }
    public void setOldStatus(String oldStatus) { this.oldStatus = oldStatus; }
    public String getNewStatus() { return newStatus; }
    public void setNewStatus(String newStatus) { this.newStatus = newStatus; }
    public String getChangedBy() { return changedBy; }
    public void setChangedBy(String changedBy) { this.changedBy = changedBy; }
    public Instant getChangedAt() { return changedAt; }
    public void setChangedAt(Instant changedAt) { this.changedAt = changedAt; }
    public String getComment() { return comment; }
    public void setComment(String comment) { this.comment = comment; }
}
