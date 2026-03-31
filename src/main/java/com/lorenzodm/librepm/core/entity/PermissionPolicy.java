package com.lorenzodm.librepm.core.entity;

import jakarta.persistence.*;

/**
 * Defines a permission policy, linking a role to a specific permission.
 * This entity allows for a granular definition of what each role can do.
 *
 * @author Lorenzo DM
 * @since 1.0.0
 * @version 1.0.0
 */
@Entity
@Table(name = "permission_policies", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"role_id", "permission"})
})
public class PermissionPolicy extends BaseSyncEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "role_id", nullable = false)
    private Role role;

    @Column(nullable = false, length = 100)
    private String permission; // e.g., "task:create", "project:delete"

    public PermissionPolicy() {
        super();
    }

    // Getters and Setters
    public Role getRole() { return role; }
    public void setRole(Role role) { this.role = role; }
    public String getPermission() { return permission; }
    public void setPermission(String permission) { this.permission = permission; }
}
