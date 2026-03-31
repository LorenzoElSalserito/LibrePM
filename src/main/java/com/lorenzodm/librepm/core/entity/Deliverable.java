package com.lorenzodm.librepm.core.entity;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Represents a project deliverable, a tangible or intangible good or service produced as a result of a project.
 *
 * @author Lorenzo DM
 * @since 1.0.0
 * @version 1.0.0
 */
@Entity
@Table(name = "deliverables", indexes = {
    @Index(name = "idx_deliverable_project", columnList = "project_id")
})
public class Deliverable extends BaseSyncEntity {

    public enum RiskStatus { OK, AT_RISK, BLOCKED }

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "project_id", nullable = false)
    private Project project;

    @Column(nullable = false, length = 255)
    private String name;

    @Column(length = 1000)
    private String description;

    @Column
    private LocalDate dueDate;

    @Column(nullable = false)
    private int progress; // 0-100

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private RiskStatus riskStatus = RiskStatus.OK;

    @Column
    private LocalDateTime completedAt;

    public Deliverable() {
        super();
    }

    // Getters and Setters
    public Project getProject() { return project; }
    public void setProject(Project project) { this.project = project; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public LocalDate getDueDate() { return dueDate; }
    public void setDueDate(LocalDate dueDate) { this.dueDate = dueDate; }
    public int getProgress() { return progress; }
    public void setProgress(int progress) { this.progress = progress; }
    public RiskStatus getRiskStatus() { return riskStatus; }
    public void setRiskStatus(RiskStatus riskStatus) { this.riskStatus = riskStatus; }
    public LocalDateTime getCompletedAt() { return completedAt; }
    public void setCompletedAt(LocalDateTime completedAt) { this.completedAt = completedAt; }
}
