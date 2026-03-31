package com.lorenzodm.librepm.core.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * Represents a snapshot of a task's state within a baseline.
 * This allows for detailed comparison between the planned state (baseline) and the current state.
 *
 * @author Lorenzo DM
 * @since 1.0.0
 * @version 1.0.0
 */
@Entity
@Table(name = "baseline_task_snapshots", indexes = {
    @Index(name = "idx_snapshot_baseline", columnList = "baseline_id"),
    @Index(name = "idx_snapshot_task", columnList = "task_id")
})
public class BaselineTaskSnapshot extends BaseSyncEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "baseline_id", nullable = false)
    private Baseline baseline;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "task_id", nullable = false)
    private Task task;

    @Column
    private LocalDateTime plannedStart;

    @Column
    private LocalDateTime plannedFinish;

    @Column
    private Integer estimatedEffort;

    public BaselineTaskSnapshot() {
        super();
    }

    // Getters and Setters
    public Baseline getBaseline() { return baseline; }
    public void setBaseline(Baseline baseline) { this.baseline = baseline; }
    public Task getTask() { return task; }
    public void setTask(Task task) { this.task = task; }
    public LocalDateTime getPlannedStart() { return plannedStart; }
    public void setPlannedStart(LocalDateTime plannedStart) { this.plannedStart = plannedStart; }
    public LocalDateTime getPlannedFinish() { return plannedFinish; }
    public void setPlannedFinish(LocalDateTime plannedFinish) { this.plannedFinish = plannedFinish; }
    public Integer getEstimatedEffort() { return estimatedEffort; }
    public void setEstimatedEffort(Integer estimatedEffort) { this.estimatedEffort = estimatedEffort; }
}
