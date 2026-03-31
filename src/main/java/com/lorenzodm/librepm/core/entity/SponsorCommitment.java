package com.lorenzodm.librepm.core.entity;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "sponsor_commitments")
public class SponsorCommitment {

    @Id
    @Column(name = "id", nullable = false, updatable = false)
    private String id;

    @Column(name = "sponsor_id")
    private String sponsorId;

    @Column(name = "project_id")
    private String projectId;

    @Column(name = "description")
    private String description;

    @Column(name = "committed_amount")
    private Double committedAmount;

    @Column(name = "currency")
    private String currency = "EUR";

    @Column(name = "status")
    private String status = "PROPOSED";

    @Column(name = "agreement_date")
    private LocalDate agreementDate;

    @Column(name = "agreement_asset_id")
    private String agreementAssetId;

    public SponsorCommitment() {
        this.id = UUID.randomUUID().toString();
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getSponsorId() {
        return sponsorId;
    }

    public void setSponsorId(String sponsorId) {
        this.sponsorId = sponsorId;
    }

    public String getProjectId() {
        return projectId;
    }

    public void setProjectId(String projectId) {
        this.projectId = projectId;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Double getCommittedAmount() {
        return committedAmount;
    }

    public void setCommittedAmount(Double committedAmount) {
        this.committedAmount = committedAmount;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public LocalDate getAgreementDate() {
        return agreementDate;
    }

    public void setAgreementDate(LocalDate agreementDate) {
        this.agreementDate = agreementDate;
    }

    public String getAgreementAssetId() {
        return agreementAssetId;
    }

    public void setAgreementAssetId(String agreementAssetId) {
        this.agreementAssetId = agreementAssetId;
    }
}
