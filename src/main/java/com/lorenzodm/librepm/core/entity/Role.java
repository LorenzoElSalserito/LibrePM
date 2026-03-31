package com.lorenzodm.librepm.core.entity;

import jakarta.persistence.*;

/**
 * Represents a role within the system for Role-Based Access Control (RBAC).
 * Roles are assigned to users to grant them specific permissions.
 *
 * @author Lorenzo DM
 * @since 1.0.0
 * @version 1.0.0
 */
@Entity
@Table(name = "roles")
public class Role extends BaseSyncEntity {

    @Column(nullable = false, unique = true, length = 50)
    private String name;

    @Column(length = 255)
    private String description;

    public Role() {
        super();
    }

    public Role(String name) {
        this.name = name;
    }

    // Getters and Setters
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
}
