package com.lorenzodm.librepm.core.entity;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "import_mapping_profiles")
public class ImportMappingProfile {

    @Id
    @Column(length = 36)
    private String id;

    @Column(nullable = false, length = 128)
    private String name;

    @Column(name = "entity_type", nullable = false, length = 64)
    private String entityType;

    @Column(name = "mapping_json", nullable = false, columnDefinition = "TEXT")
    private String mappingJson;

    @Column(name = "created_by", length = 36)
    private String createdBy;

    @Column(name = "created_at")
    private Instant createdAt;

    public ImportMappingProfile() {
        this.id = UUID.randomUUID().toString();
        this.createdAt = Instant.now();
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getEntityType() { return entityType; }
    public void setEntityType(String entityType) { this.entityType = entityType; }
    public String getMappingJson() { return mappingJson; }
    public void setMappingJson(String mappingJson) { this.mappingJson = mappingJson; }
    public String getCreatedBy() { return createdBy; }
    public void setCreatedBy(String createdBy) { this.createdBy = createdBy; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
}
