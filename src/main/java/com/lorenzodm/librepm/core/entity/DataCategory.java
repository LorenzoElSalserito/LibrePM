package com.lorenzodm.librepm.core.entity;

import jakarta.persistence.*;
import java.util.UUID;

@Entity
@Table(name = "data_categories")
public class DataCategory {

    @Id
    @Column(name = "id", nullable = false, updatable = false)
    private String id;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "sensitivity_level")
    private String sensitivityLevel = "NORMAL";

    @Column(name = "default_retention_days")
    private Integer defaultRetentionDays;

    @Column(name = "processing_purposes", columnDefinition = "TEXT")
    private String processingPurposes;

    public DataCategory() {
        this.id = UUID.randomUUID().toString();
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getSensitivityLevel() {
        return sensitivityLevel;
    }

    public void setSensitivityLevel(String sensitivityLevel) {
        this.sensitivityLevel = sensitivityLevel;
    }

    public Integer getDefaultRetentionDays() {
        return defaultRetentionDays;
    }

    public void setDefaultRetentionDays(Integer defaultRetentionDays) {
        this.defaultRetentionDays = defaultRetentionDays;
    }

    public String getProcessingPurposes() {
        return processingPurposes;
    }

    public void setProcessingPurposes(String processingPurposes) {
        this.processingPurposes = processingPurposes;
    }
}
