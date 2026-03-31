package com.lorenzodm.librepm.core.entity;

import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Represents an Objective and Key Result (OKR) associated with a project.
 * OKRs define measurable goals and track their achievement.
 *
 * @author Lorenzo DM
 * @since 1.0.0
 * @version 1.0.0
 */
@Entity
@Table(name = "okrs", indexes = {
    @Index(name = "idx_okr_project", columnList = "project_id")
})
public class Okr extends BaseSyncEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "project_id", nullable = false)
    private Project project;

    @Column(nullable = false, length = 255)
    private String objective;

    @OneToMany(mappedBy = "okr", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<SuccessMetric> keyResults = new ArrayList<>();

    public Okr() {
        super();
    }

    // Getters and Setters
    public Project getProject() { return project; }
    public void setProject(Project project) { this.project = project; }
    public String getObjective() { return objective; }
    public void setObjective(String objective) { this.objective = objective; }
    public List<SuccessMetric> getKeyResults() { return keyResults; }
    public void setKeyResults(List<SuccessMetric> keyResults) { this.keyResults = keyResults; }
}
