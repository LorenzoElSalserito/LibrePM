package com.lorenzodm.librepm.core.entity;

import jakarta.persistence.*;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "status_reviews")
public class StatusReview {

    public enum TrafficLight { GREEN, YELLOW, RED }

    @Id
    @Column(length = 36)
    private String id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id", nullable = false)
    private Project project;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reviewer_id", nullable = false)
    private User reviewer;

    @Column(name = "review_date", nullable = false)
    private LocalDate reviewDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "overall_status", nullable = false, length = 16)
    private TrafficLight overallStatus;

    @Enumerated(EnumType.STRING)
    @Column(name = "schedule_status", length = 16)
    private TrafficLight scheduleStatus;

    @Enumerated(EnumType.STRING)
    @Column(name = "budget_status", length = 16)
    private TrafficLight budgetStatus;

    @Enumerated(EnumType.STRING)
    @Column(name = "risk_status", length = 16)
    private TrafficLight riskStatus;

    @Column(columnDefinition = "TEXT")
    private String summary;

    @Column(columnDefinition = "TEXT")
    private String achievements;

    @Column(columnDefinition = "TEXT")
    private String blockers;

    @Column(name = "next_actions", columnDefinition = "TEXT")
    private String nextActions;

    @Column(name = "created_at")
    private Instant createdAt;

    public StatusReview() {
        this.id = UUID.randomUUID().toString();
        this.createdAt = Instant.now();
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public Project getProject() { return project; }
    public void setProject(Project project) { this.project = project; }
    public User getReviewer() { return reviewer; }
    public void setReviewer(User reviewer) { this.reviewer = reviewer; }
    public LocalDate getReviewDate() { return reviewDate; }
    public void setReviewDate(LocalDate reviewDate) { this.reviewDate = reviewDate; }
    public TrafficLight getOverallStatus() { return overallStatus; }
    public void setOverallStatus(TrafficLight overallStatus) { this.overallStatus = overallStatus; }
    public TrafficLight getScheduleStatus() { return scheduleStatus; }
    public void setScheduleStatus(TrafficLight scheduleStatus) { this.scheduleStatus = scheduleStatus; }
    public TrafficLight getBudgetStatus() { return budgetStatus; }
    public void setBudgetStatus(TrafficLight budgetStatus) { this.budgetStatus = budgetStatus; }
    public TrafficLight getRiskStatus() { return riskStatus; }
    public void setRiskStatus(TrafficLight riskStatus) { this.riskStatus = riskStatus; }
    public String getSummary() { return summary; }
    public void setSummary(String summary) { this.summary = summary; }
    public String getAchievements() { return achievements; }
    public void setAchievements(String achievements) { this.achievements = achievements; }
    public String getBlockers() { return blockers; }
    public void setBlockers(String blockers) { this.blockers = blockers; }
    public String getNextActions() { return nextActions; }
    public void setNextActions(String nextActions) { this.nextActions = nextActions; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
}
