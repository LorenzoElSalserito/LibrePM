package com.lorenzodm.librepm.core.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;

import java.time.Instant;

/**
 * Represents a remote identity linked to a local user via OIDC/OAuth2 providers.
 * Prepared for future SSO integration (Google, Microsoft, Keycloak, generic OIDC).
 *
 * @author Lorenzo DM
 * @since 0.9.1
 */
@Entity
@Table(name = "remote_identities",
        uniqueConstraints = @UniqueConstraint(columnNames = {"provider", "provider_user_id"}))
@SQLDelete(sql = "UPDATE remote_identities SET deleted = TRUE WHERE id = ?")
@Where(clause = "deleted = FALSE")
public class RemoteIdentity extends BaseSyncEntity {

    public enum Provider {
        GOOGLE,
        MICROSOFT,
        KEYCLOAK,
        GENERIC_OIDC
    }

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @NotBlank
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 64)
    private Provider provider;

    @NotBlank
    @Column(name = "provider_user_id", nullable = false, length = 255)
    private String providerUserId;

    @Column(length = 255)
    private String email;

    @Column(name = "bound_at")
    private Instant boundAt;

    @Column(name = "revoked_at")
    private Instant revokedAt;

    @Column(nullable = false)
    private boolean deleted = false;

    public RemoteIdentity() {
        super();
        this.boundAt = Instant.now();
    }

    // Getters and Setters
    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }
    public Provider getProvider() { return provider; }
    public void setProvider(Provider provider) { this.provider = provider; }
    public String getProviderUserId() { return providerUserId; }
    public void setProviderUserId(String providerUserId) { this.providerUserId = providerUserId; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public Instant getBoundAt() { return boundAt; }
    public void setBoundAt(Instant boundAt) { this.boundAt = boundAt; }
    public Instant getRevokedAt() { return revokedAt; }
    public void setRevokedAt(Instant revokedAt) { this.revokedAt = revokedAt; }
    public boolean isDeleted() { return deleted; }
    public void setDeleted(boolean deleted) { this.deleted = deleted; }
}
