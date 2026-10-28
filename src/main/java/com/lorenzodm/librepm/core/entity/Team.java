package com.lorenzodm.librepm.core.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.time.Instant;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

/**
 * Entity Team - Rappresenta un team di lavoro (PRD-TEAM)
 *
 * @author Lorenzo DM
 * @since 1.0.0
 */
@Entity
@Table(name = "teams", indexes = {
        @Index(name = "idx_team_owner", columnList = "owner_id")
})
public class Team {

    @Id
    @Column(length = 36)
    private String id;

    @NotBlank(message = "Nome team obbligatorio")
    @Size(min = 1, max = 100)
    @Column(nullable = false, length = 100)
    private String name;

    @Column(length = 500)
    private String description;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_id", nullable = false)
    private User owner;

    @OneToMany(mappedBy = "team", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<TeamMember> members = new HashSet<>();

    @OneToMany(mappedBy = "team")
    private Set<Project> projects = new HashSet<>();

    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    @Column(nullable = false)
    private Instant updatedAt;

    public Team() {
        this.id = UUID.randomUUID().toString();
    }

    public Team(String name, User owner) {
        this();
        this.name = name;
        this.owner = owner;
    }

    // Getters & Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public User getOwner() { return owner; }
    public void setOwner(User owner) { this.owner = owner; }
    public Set<TeamMember> getMembers() { return members; }
    public void setMembers(Set<TeamMember> members) { this.members = members; }
    public Set<Project> getProjects() { return projects; }
    public void setProjects(Set<Project> projects) { this.projects = projects; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Team team)) return false;
        return Objects.equals(id, team.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    // Hibernate's @CreationTimestamp/@UpdateTimestamp truncate to whole seconds on
    // converter-mapped columns: same-second notes and messages lost their order.
    @PrePersist
    protected void initAuditTimestamps() {
        Instant now = Instant.now();
        createdAt = now;
        updatedAt = now;
    }

    @PreUpdate
    protected void refreshUpdatedAt() {
        updatedAt = Instant.now();
    }
}
