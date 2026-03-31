package com.lorenzodm.librepm.core.entity;

import jakarta.persistence.*;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "obligation_records")
public class ObligationRecord {

    @Id
    @Column(name = "id", nullable = false, updatable = false)
    private String id;

    @Column(name = "project_id")
    private String projectId;

    @Column(name = "title", nullable = false)
    private String title;

    @Column(name = "description")
    private String description;

    @Column(name = "type")
    private String type;

    @Column(name = "deadline")
    private LocalDate deadline;

    @Column(name = "responsible_user_id")
    private String responsibleUserId;

    @Column(name = "status")
    private String status = "PENDING";

    @Column(name = "linked_deliverable_id")
    private String linkedDeliverableId;

    @Column(name = "linked_budget_line_id")
    private String linkedBudgetLineId;

    @Column(name = "notes")
    private String notes;

    @Column(name = "created_at", updatable = false)
    private Instant createdAt;

    public ObligationRecord() {
        this.id = UUID.randomUUID().toString();
        this.createdAt = Instant.now();
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

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public LocalDate getDeadline() {
        return deadline;
    }

    public void setDeadline(LocalDate deadline) {
        this.deadline = deadline;
    }

    public String getResponsibleUserId() {
        return responsibleUserId;
    }

    public void setResponsibleUserId(String responsibleUserId) {
        this.responsibleUserId = responsibleUserId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getLinkedDeliverableId() {
        return linkedDeliverableId;
    }

    public void setLinkedDeliverableId(String linkedDeliverableId) {
        this.linkedDeliverableId = linkedDeliverableId;
    }

    public String getLinkedBudgetLineId() {
        return linkedBudgetLineId;
    }

    public void setLinkedBudgetLineId(String linkedBudgetLineId) {
        this.linkedBudgetLineId = linkedBudgetLineId;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }
}
