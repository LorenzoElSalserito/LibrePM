package com.lorenzodm.librepm.core.entity;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "reporting_periods")
public class ReportingPeriod {

    @Id
    @Column(name = "id", nullable = false, updatable = false)
    private String id;

    @Column(name = "project_id")
    private String projectId;

    @Column(name = "name")
    private String name;

    @Column(name = "period_start")
    private LocalDate periodStart;

    @Column(name = "period_end")
    private LocalDate periodEnd;

    @Column(name = "due_date")
    private LocalDate dueDate;

    @Column(name = "status")
    private String status = "UPCOMING";

    @Column(name = "submission_asset_id")
    private String submissionAssetId;

    @Column(name = "notes")
    private String notes;

    public ReportingPeriod() {
        this.id = UUID.randomUUID().toString();
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getProjectId() {
        return projectId;
    }

    public void setProjectId(String projectId) {
        this.projectId = projectId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public LocalDate getPeriodStart() {
        return periodStart;
    }

    public void setPeriodStart(LocalDate periodStart) {
        this.periodStart = periodStart;
    }

    public LocalDate getPeriodEnd() {
        return periodEnd;
    }

    public void setPeriodEnd(LocalDate periodEnd) {
        this.periodEnd = periodEnd;
    }

    public LocalDate getDueDate() {
        return dueDate;
    }

    public void setDueDate(LocalDate dueDate) {
        this.dueDate = dueDate;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getSubmissionAssetId() {
        return submissionAssetId;
    }

    public void setSubmissionAssetId(String submissionAssetId) {
        this.submissionAssetId = submissionAssetId;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }
}
