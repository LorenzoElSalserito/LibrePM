package com.lorenzodm.librepm.core.entity;

import jakarta.persistence.*;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "donations")
public class Donation {

    @Id
    @Column(name = "id", nullable = false, updatable = false)
    private String id;

    @Column(name = "donor_id")
    private String donorId;

    @Column(name = "project_id")
    private String projectId;

    @Column(name = "amount")
    private Double amount;

    @Column(name = "currency")
    private String currency = "EUR";

    @Column(name = "donation_date")
    private LocalDate donationDate;

    @Column(name = "is_restricted")
    private Boolean isRestricted = false;

    @Column(name = "restriction_description")
    private String restrictionDescription;

    @Column(name = "receipt_asset_id")
    private String receiptAssetId;

    @Column(name = "notes")
    private String notes;

    @Column(name = "created_at", updatable = false)
    private Instant createdAt;

    public Donation() {
        this.id = UUID.randomUUID().toString();
        this.createdAt = Instant.now();
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getDonorId() {
        return donorId;
    }

    public void setDonorId(String donorId) {
        this.donorId = donorId;
    }

    public String getProjectId() {
        return projectId;
    }

    public void setProjectId(String projectId) {
        this.projectId = projectId;
    }

    public Double getAmount() {
        return amount;
    }

    public void setAmount(Double amount) {
        this.amount = amount;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public LocalDate getDonationDate() {
        return donationDate;
    }

    public void setDonationDate(LocalDate donationDate) {
        this.donationDate = donationDate;
    }

    public Boolean getIsRestricted() {
        return isRestricted;
    }

    public void setIsRestricted(Boolean isRestricted) {
        this.isRestricted = isRestricted;
    }

    public String getRestrictionDescription() {
        return restrictionDescription;
    }

    public void setRestrictionDescription(String restrictionDescription) {
        this.restrictionDescription = restrictionDescription;
    }

    public String getReceiptAssetId() {
        return receiptAssetId;
    }

    public void setReceiptAssetId(String receiptAssetId) {
        this.receiptAssetId = receiptAssetId;
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
