package com.lorenzodm.librepm.core.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;

import java.time.Instant;

/**
 * Represents an external contributor with limited, scoped access.
 *
 * @author Lorenzo DM
 * @since 0.9.1
 */
@Entity
@Table(name = "external_contributors")
@SQLDelete(sql = "UPDATE external_contributors SET deleted_at = CURRENT_TIMESTAMP WHERE id = ?")
@Where(clause = "deleted_at IS NULL")
public class ExternalContributor extends BaseSyncEntity {

    public enum Scope {
        PROJECT,
        DELIVERABLE,
        REVIEW
    }

    @NotBlank
    @Column(name = "display_name", nullable = false, length = 128)
    private String displayName;

    @Column(length = 255)
    private String email;

    @Column(length = 255)
    private String organization;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "role_id")
    private Role role;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private Scope scope = Scope.PROJECT;

    @Column(name = "scope_entity_id", length = 36)
    private String scopeEntityId;

    @Column(name = "access_token_hash", length = 128)
    private String accessTokenHash;

    @Column(name = "access_expires_at")
    private Instant accessExpiresAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by")
    private User createdBy;

    public ExternalContributor() {
        super();
    }

    // Getters and Setters
    public String getDisplayName() { return displayName; }
    public void setDisplayName(String displayName) { this.displayName = displayName; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getOrganization() { return organization; }
    public void setOrganization(String organization) { this.organization = organization; }
    public Role getRole() { return role; }
    public void setRole(Role role) { this.role = role; }
    public Scope getScope() { return scope; }
    public void setScope(Scope scope) { this.scope = scope; }
    public String getScopeEntityId() { return scopeEntityId; }
    public void setScopeEntityId(String scopeEntityId) { this.scopeEntityId = scopeEntityId; }
    public String getAccessTokenHash() { return accessTokenHash; }
    public void setAccessTokenHash(String accessTokenHash) { this.accessTokenHash = accessTokenHash; }
    public Instant getAccessExpiresAt() { return accessExpiresAt; }
    public void setAccessExpiresAt(Instant accessExpiresAt) { this.accessExpiresAt = accessExpiresAt; }
    public User getCreatedBy() { return createdBy; }
    public void setCreatedBy(User createdBy) { this.createdBy = createdBy; }
}
