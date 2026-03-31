package com.lorenzodm.librepm.core.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Represents an immutable snapshot of the project plan at a specific point in time.
 * Baselines are used to track variance and forecast project performance.
 *
 * @author Lorenzo DM
 * @since 1.0.0
 * @version 1.0.0
 */
@Entity
@Table(name = "baselines", indexes = {
    @Index(name = "idx_baseline_project", columnList = "project_id")
})
public class Baseline extends BaseSyncEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "project_id", nullable = false)
    private Project project;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private LocalDateTime snapshotDate;

    @Column(name = "is_frozen")
    private boolean frozen = true;

    @Column(name = "budget_snapshot", columnDefinition = "TEXT")
    private String budgetSnapshot;

    @Column(name = "deliverable_snapshot", columnDefinition = "TEXT")
    private String deliverableSnapshot;

    @OneToMany(mappedBy = "baseline", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<BaselineTaskSnapshot> taskSnapshots = new ArrayList<>();

    public Baseline() {
        super();
    }

    // Getters and Setters
    public Project getProject() { return project; }
    public void setProject(Project project) { this.project = project; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public LocalDateTime getSnapshotDate() { return snapshotDate; }
    public void setSnapshotDate(LocalDateTime snapshotDate) { this.snapshotDate = snapshotDate; }
    public List<BaselineTaskSnapshot> getTaskSnapshots() { return taskSnapshots; }
    public void setTaskSnapshots(List<BaselineTaskSnapshot> taskSnapshots) { this.taskSnapshots = taskSnapshots; }
    public boolean isFrozen() { return frozen; }
    public void setFrozen(boolean frozen) { this.frozen = frozen; }
    public String getBudgetSnapshot() { return budgetSnapshot; }
    public void setBudgetSnapshot(String budgetSnapshot) { this.budgetSnapshot = budgetSnapshot; }
    public String getDeliverableSnapshot() { return deliverableSnapshot; }
    public void setDeliverableSnapshot(String deliverableSnapshot) { this.deliverableSnapshot = deliverableSnapshot; }
}
