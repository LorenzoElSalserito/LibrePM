package com.lorenzodm.librepm.core.entity;

import jakarta.persistence.*;

/**
 * Represents the project charter, a key document for the executive dashboard.
 * It contains high-level information about the project's goals, scope, and stakeholders.
 *
 * @author Lorenzo DM
 * @since 1.0.0
 * @version 1.0.0
 */
@Entity
@Table(name = "project_charters")
public class ProjectCharter extends BaseSyncEntity {

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "project_id", nullable = false)
    private Project project;

    @Column(length = 255)
    private String sponsor; // PRD-17-FR-002: team panel sponsor

    @Column(length = 255)
    private String projectManager; // PRD-17-FR-002: team panel PM

    @Column(columnDefinition = "TEXT")
    private String objectives; // PRD-17-FR-003: goals/objectives

    @Column(columnDefinition = "TEXT")
    private String problemStatement;

    @Column(columnDefinition = "TEXT")
    private String businessCase;

    public ProjectCharter() {
        super();
    }

    // Getters and Setters
    public Project getProject() { return project; }
    public void setProject(Project project) { this.project = project; }
    public String getSponsor() { return sponsor; }
    public void setSponsor(String sponsor) { this.sponsor = sponsor; }
    public String getProjectManager() { return projectManager; }
    public void setProjectManager(String projectManager) { this.projectManager = projectManager; }
    public String getObjectives() { return objectives; }
    public void setObjectives(String objectives) { this.objectives = objectives; }
    public String getProblemStatement() { return problemStatement; }
    public void setProblemStatement(String problemStatement) { this.problemStatement = problemStatement; }
    public String getBusinessCase() { return businessCase; }
    public void setBusinessCase(String businessCase) { this.businessCase = businessCase; }
}
