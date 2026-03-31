package com.lorenzodm.librepm.core.entity;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "stakeholders")
public class Stakeholder {

    @Id
    @Column(name = "id", nullable = false, updatable = false)
    private String id;

    @Column(name = "project_id")
    private String projectId;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "organization")
    private String organization;

    @Column(name = "role_description")
    private String roleDescription;

    @Column(name = "influence_level")
    private String influenceLevel;

    @Column(name = "interest_level")
    private String interestLevel;

    @Column(name = "engagement_strategy")
    private String engagementStrategy;

    @Column(name = "channel")
    private String channel;

    @Column(name = "notes")
    private String notes;

    @Column(name = "created_at", updatable = false)
    private Instant createdAt;

    public Stakeholder() {
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

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getOrganization() {
        return organization;
    }

    public void setOrganization(String organization) {
        this.organization = organization;
    }

    public String getRoleDescription() {
        return roleDescription;
    }

    public void setRoleDescription(String roleDescription) {
        this.roleDescription = roleDescription;
    }

    public String getInfluenceLevel() {
        return influenceLevel;
    }

    public void setInfluenceLevel(String influenceLevel) {
        this.influenceLevel = influenceLevel;
    }

    public String getInterestLevel() {
        return interestLevel;
    }

    public void setInterestLevel(String interestLevel) {
        this.interestLevel = interestLevel;
    }

    public String getEngagementStrategy() {
        return engagementStrategy;
    }

    public void setEngagementStrategy(String engagementStrategy) {
        this.engagementStrategy = engagementStrategy;
    }

    public String getChannel() {
        return channel;
    }

    public void setChannel(String channel) {
        this.channel = channel;
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
