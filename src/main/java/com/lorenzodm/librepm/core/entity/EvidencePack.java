package com.lorenzodm.librepm.core.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "evidence_packs")
@SQLDelete(sql = "UPDATE evidence_packs SET deleted_at = CURRENT_TIMESTAMP WHERE id = ?")
@Where(clause = "deleted_at IS NULL")
public class EvidencePack {

    public enum PackType {
        SUBMISSION, AUDIT, REPORT, SPONSOR_REVIEW
    }

    public enum Status {
        DRAFT, FINALIZED, EXPORTED
    }

    @Id
    @Column(length = 36)
    private String id;

    @NotBlank
    @Column(nullable = false, length = 255)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id")
    private Project project;

    @Enumerated(EnumType.STRING)
    @Column(name = "pack_type", length = 32)
    private PackType packType;

    @Enumerated(EnumType.STRING)
    @Column(length = 32)
    private Status status = Status.DRAFT;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by")
    private User createdBy;

    @Column(name = "created_at")
    private Instant createdAt;

    @Column(name = "finalized_at")
    private Instant finalizedAt;

    @Column(name = "deleted_at")
    private Instant deletedAt;

    public EvidencePack() {
        this.id = UUID.randomUUID().toString();
        this.createdAt = Instant.now();
    }

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public Project getProject() { return project; }
    public void setProject(Project project) { this.project = project; }
    public PackType getPackType() { return packType; }
    public void setPackType(PackType packType) { this.packType = packType; }
    public Status getStatus() { return status; }
    public void setStatus(Status status) { this.status = status; }
    public User getCreatedBy() { return createdBy; }
    public void setCreatedBy(User createdBy) { this.createdBy = createdBy; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
    public Instant getFinalizedAt() { return finalizedAt; }
    public void setFinalizedAt(Instant finalizedAt) { this.finalizedAt = finalizedAt; }
    public Instant getDeletedAt() { return deletedAt; }
    public void setDeletedAt(Instant deletedAt) { this.deletedAt = deletedAt; }
}
