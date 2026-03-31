package com.lorenzodm.librepm.core.entity;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "workspace_profiles")
public class WorkspaceProfile {

    @Id
    @Column(length = 36)
    private String id;

    @Column(nullable = false, length = 64)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "modules_json", nullable = false, columnDefinition = "TEXT")
    private String modulesJson;

    @Column(name = "nav_items_json", columnDefinition = "TEXT")
    private String navItemsJson;

    @Column(name = "is_system")
    private boolean system = false;

    @Column(name = "created_at")
    private Instant createdAt;

    public WorkspaceProfile() {
        this.id = UUID.randomUUID().toString();
        this.createdAt = Instant.now();
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getModulesJson() { return modulesJson; }
    public void setModulesJson(String modulesJson) { this.modulesJson = modulesJson; }
    public String getNavItemsJson() { return navItemsJson; }
    public void setNavItemsJson(String navItemsJson) { this.navItemsJson = navItemsJson; }
    public boolean isSystem() { return system; }
    public void setSystem(boolean system) { this.system = system; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
}
