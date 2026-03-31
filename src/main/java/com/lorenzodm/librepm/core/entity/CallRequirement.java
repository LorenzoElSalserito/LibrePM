package com.lorenzodm.librepm.core.entity;

import jakarta.persistence.*;
import java.util.UUID;

@Entity
@Table(name = "call_requirements")
public class CallRequirement {

    @Id
    @Column(name = "id", nullable = false, updatable = false)
    private String id;

    @Column(name = "call_id")
    private String callId;

    @Column(name = "description")
    private String description;

    @Column(name = "requirement_type")
    private String requirementType;

    @Column(name = "is_met")
    private Boolean isMet = false;

    @Column(name = "evidence_note")
    private String evidenceNote;

    @Column(name = "sort_order")
    private Integer sortOrder = 0;

    public CallRequirement() {
        this.id = UUID.randomUUID().toString();
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getCallId() {
        return callId;
    }

    public void setCallId(String callId) {
        this.callId = callId;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getRequirementType() {
        return requirementType;
    }

    public void setRequirementType(String requirementType) {
        this.requirementType = requirementType;
    }

    public Boolean getIsMet() {
        return isMet;
    }

    public void setIsMet(Boolean isMet) {
        this.isMet = isMet;
    }

    public String getEvidenceNote() {
        return evidenceNote;
    }

    public void setEvidenceNote(String evidenceNote) {
        this.evidenceNote = evidenceNote;
    }

    public Integer getSortOrder() {
        return sortOrder;
    }

    public void setSortOrder(Integer sortOrder) {
        this.sortOrder = sortOrder;
    }
}
