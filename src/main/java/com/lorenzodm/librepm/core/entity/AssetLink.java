package com.lorenzodm.librepm.core.entity;

import jakarta.persistence.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "asset_links")
public class AssetLink {

    @Id
    @Column(length = 36)
    private String id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "asset_id", nullable = false)
    private Asset asset;

    @Column(name = "linked_entity_type", nullable = false, length = 64)
    private String linkedEntityType;

    @Column(name = "linked_entity_id", nullable = false, length = 36)
    private String linkedEntityId;

    @Column(name = "created_at")
    private Instant createdAt;

    public AssetLink() {
        this.id = UUID.randomUUID().toString();
        this.createdAt = Instant.now();
    }

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public Asset getAsset() { return asset; }
    public void setAsset(Asset asset) { this.asset = asset; }
    public String getLinkedEntityType() { return linkedEntityType; }
    public void setLinkedEntityType(String linkedEntityType) { this.linkedEntityType = linkedEntityType; }
    public String getLinkedEntityId() { return linkedEntityId; }
    public void setLinkedEntityId(String linkedEntityId) { this.linkedEntityId = linkedEntityId; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
}
