package com.lorenzodm.librepm.core.entity;

import jakarta.persistence.*;

/**
 * Represents an entry in the project's risk register.
 * It tracks potential risks, their probability, impact, and mitigation strategies.
 *
 * @author Lorenzo DM
 * @since 1.0.0
 * @version 1.0.0
 */
@Entity
@Table(name = "risk_register_entries", indexes = {
    @Index(name = "idx_risk_project", columnList = "project_id")
})
public class RiskRegisterEntry extends BaseSyncEntity {

    public enum RiskLevel {
        LOW,
        MEDIUM,
        HIGH,
        CRITICAL
    }

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "project_id", nullable = false)
    private Project project;

    @Column(nullable = false, length = 255)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private RiskLevel probability;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private RiskLevel impact;

    @Column(columnDefinition = "TEXT")
    private String mitigationStrategy;

    public RiskRegisterEntry() {
        super();
    }

    // Getters and Setters
    public Project getProject() { return project; }
    public void setProject(Project project) { this.project = project; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public RiskLevel getProbability() { return probability; }
    public void setProbability(RiskLevel probability) { this.probability = probability; }
    public RiskLevel getImpact() { return impact; }
    public void setImpact(RiskLevel impact) { this.impact = impact; }
    public String getMitigationStrategy() { return mitigationStrategy; }
    public void setMitigationStrategy(String mitigationStrategy) { this.mitigationStrategy = mitigationStrategy; }
}
