package com.lorenzodm.librepm.core.entity;

import jakarta.persistence.*;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "actual_cost_records")
public class ActualCostRecord {

    @Id
    @Column(name = "id", nullable = false, updatable = false)
    private String id;

    @Column(name = "budget_line_id", nullable = false)
    private String budgetLineId;

    @Column(name = "description", nullable = false)
    private String description;

    @Column(name = "amount")
    private double amount;

    @Column(name = "currency")
    private String currency = "EUR";

    @Column(name = "cost_date", nullable = false)
    private LocalDate costDate;

    @Column(name = "evidence_asset_id")
    private String evidenceAssetId;

    @Column(name = "recorded_by")
    private String recordedBy;

    @Column(name = "created_at", updatable = false)
    private Instant createdAt;

    public ActualCostRecord() {
        this.id = UUID.randomUUID().toString();
        this.createdAt = Instant.now();
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getBudgetLineId() {
        return budgetLineId;
    }

    public void setBudgetLineId(String budgetLineId) {
        this.budgetLineId = budgetLineId;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public LocalDate getCostDate() {
        return costDate;
    }

    public void setCostDate(LocalDate costDate) {
        this.costDate = costDate;
    }

    public String getEvidenceAssetId() {
        return evidenceAssetId;
    }

    public void setEvidenceAssetId(String evidenceAssetId) {
        this.evidenceAssetId = evidenceAssetId;
    }

    public String getRecordedBy() {
        return recordedBy;
    }

    public void setRecordedBy(String recordedBy) {
        this.recordedBy = recordedBy;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }
}
