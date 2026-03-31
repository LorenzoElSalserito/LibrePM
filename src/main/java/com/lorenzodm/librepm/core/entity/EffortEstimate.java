package com.lorenzodm.librepm.core.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * Represents an effort estimation for a task.
 * This entity allows tracking the history of estimates, enabling variance analysis
 * between initial estimates, revised estimates, and actual effort.
 *
 * @author Lorenzo DM
 * @since 1.0.0
 * @version 1.0.0
 */
@Entity
@Table(name = "effort_estimates", indexes = {
    @Index(name = "idx_estimate_task", columnList = "task_id"),
    @Index(name = "idx_estimate_date", columnList = "estimation_date")
})
public class EffortEstimate extends BaseSyncEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "task_id", nullable = false)
    private Task task;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "estimator_id")
    private User estimator;

    @Column(nullable = false)
    private Integer estimatedMinutes;

    @Column(nullable = false)
    private LocalDateTime estimationDate;

    @Column(length = 500)
    private String rationale;

    @Column(name = "revision_number")
    private Integer revisionNumber = 1;

    @Column(name = "previous_estimate_id", length = 36)
    private String previousEstimateId;

    public EffortEstimate() {
        super();
    }

    // Getters and Setters
    public Task getTask() { return task; }
    public void setTask(Task task) { this.task = task; }
    public User getEstimator() { return estimator; }
    public void setEstimator(User estimator) { this.estimator = estimator; }
    public Integer getEstimatedMinutes() { return estimatedMinutes; }
    public void setEstimatedMinutes(Integer estimatedMinutes) { this.estimatedMinutes = estimatedMinutes; }
    public LocalDateTime getEstimationDate() { return estimationDate; }
    public void setEstimationDate(LocalDateTime estimationDate) { this.estimationDate = estimationDate; }
    public String getRationale() { return rationale; }
    public void setRationale(String rationale) { this.rationale = rationale; }

    public Integer getRevisionNumber() { return revisionNumber; }
    public void setRevisionNumber(Integer revisionNumber) { this.revisionNumber = revisionNumber; }

    public String getPreviousEstimateId() { return previousEstimateId; }
    public void setPreviousEstimateId(String previousEstimateId) { this.previousEstimateId = previousEstimateId; }
}
