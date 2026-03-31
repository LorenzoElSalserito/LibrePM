package com.lorenzodm.librepm.core.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * Represents the assignment of a user to a task.
 * This entity allows for multiple users to be assigned to a single task,
 * potentially with different roles or responsibilities.
 *
 * @author Lorenzo DM
 * @since 1.0.0
 * @version 1.0.0
 */
@Entity
@Table(name = "assignments", indexes = {
    @Index(name = "idx_assignment_task", columnList = "task_id"),
    @Index(name = "idx_assignment_user", columnList = "user_id")
})
public class Assignment extends BaseSyncEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "task_id", nullable = false)
    private Task task;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false)
    private LocalDateTime assignedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "role_id")
    private Role role; // Optional role specific to this assignment

    @Column
    private Integer estimatedEffort; // PRD-12-FR-002: effort per assignee in minutes

    public Assignment() {
        super();
    }

    // Getters and Setters
    public Task getTask() { return task; }
    public void setTask(Task task) { this.task = task; }
    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }
    public LocalDateTime getAssignedAt() { return assignedAt; }
    public void setAssignedAt(LocalDateTime assignedAt) { this.assignedAt = assignedAt; }
    public Role getRole() { return role; }
    public void setRole(Role role) { this.role = role; }
    public Integer getEstimatedEffort() { return estimatedEffort; }
    public void setEstimatedEffort(Integer estimatedEffort) { this.estimatedEffort = estimatedEffort; }
}
