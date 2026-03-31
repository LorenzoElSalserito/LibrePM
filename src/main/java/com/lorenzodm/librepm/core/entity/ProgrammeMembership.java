package com.lorenzodm.librepm.core.entity;

import jakarta.persistence.*;
import java.util.UUID;

@Entity
@Table(name = "programme_memberships")
public class ProgrammeMembership {

    @Id
    @Column(name = "id", nullable = false, updatable = false)
    private String id;

    @Column(name = "programme_id")
    private String programmeId;

    @Column(name = "project_id")
    private String projectId;

    @Column(name = "sort_order")
    private Integer sortOrder = 0;

    public ProgrammeMembership() {
        this.id = UUID.randomUUID().toString();
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getProgrammeId() {
        return programmeId;
    }

    public void setProgrammeId(String programmeId) {
        this.programmeId = programmeId;
    }

    public String getProjectId() {
        return projectId;
    }

    public void setProjectId(String projectId) {
        this.projectId = projectId;
    }

    public Integer getSortOrder() {
        return sortOrder;
    }

    public void setSortOrder(Integer sortOrder) {
        this.sortOrder = sortOrder;
    }
}
