package com.lorenzodm.librepm.core.entity;

import jakarta.persistence.Column;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.MappedSuperclass;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

/**
 * Base abstract class for all entities that require synchronization capabilities.
 * <p>
 * This class provides the fundamental fields needed for a "Last-Write-Wins" (LWW)
 * synchronization strategy, including UUIDs, timestamps for creation/updates,
 * and soft-delete support.
 * </p>
 *
 * @author Lorenzo DM
 * @since 0.5.2
 */
@MappedSuperclass
public abstract class BaseSyncEntity {

    /**
     * Unique identifier for the entity (UUID v4).
     * Using UUIDs avoids collision issues during offline creation and sync.
     */
    @Id
    @Column(length = 36)
    private String id;

    /**
     * Timestamp of when the entity was first created.
     * Managed automatically by Hibernate.
     */
    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    /**
     * Timestamp of the last modification.
     * Critical for LWW conflict resolution strategy.
     */
    @UpdateTimestamp
    @Column(nullable = false)
    private Instant updatedAt;

    /**
     * Timestamp of deletion (Soft Delete).
     * If null, the entity is active. If set, the entity is considered deleted.
     * This allows propagating deletions to other devices during sync.
     */
    @Column
    private Instant deletedAt;

    /**
     * Timestamp of the last successful synchronization with the cloud/remote server.
     */
    @Column
    private Instant lastSyncedAt;

    /**
     * Current synchronization status of the entity.
     * Possible values: LOCAL_ONLY, SYNCED, CONFLICT, PENDING, SOFT_DELETED.
     */
    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private SyncStatus syncStatus = SyncStatus.LOCAL_ONLY; // Default

    /**
     * Default constructor.
     * Initializes the ID with a new random UUID.
     */
    public BaseSyncEntity() {
        this.id = UUID.randomUUID().toString();
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }

    public Instant getDeletedAt() {
        return deletedAt;
    }

    public void setDeletedAt(Instant deletedAt) {
        this.deletedAt = deletedAt;
    }

    public Instant getLastSyncedAt() {
        return lastSyncedAt;
    }

    public void setLastSyncedAt(Instant lastSyncedAt) {
        this.lastSyncedAt = lastSyncedAt;
    }

    public SyncStatus getSyncStatus() {
        return syncStatus;
    }

    public void setSyncStatus(SyncStatus syncStatus) {
        this.syncStatus = syncStatus;
    }

    // --- LWW (Last-Write-Wins) Logic ---

    /**
     * Determines if an incoming update should be applied based on LWW strategy.
     *
     * @param remoteUpdatedAt The timestamp of the incoming remote change.
     * @return true if the remote change is newer than the local state, false otherwise.
     */
    public boolean shouldApplyUpdate(Instant remoteUpdatedAt) {
        if (remoteUpdatedAt == null) {
            return false; // No timestamp, assume unsafe to update
        }
        if (this.updatedAt == null) {
            return true; // Local has no timestamp, accept update
        }
        // Apply if remote is strictly after local
        return remoteUpdatedAt.isAfter(this.updatedAt);
    }

    /**
     * Manually updates the modification timestamp, useful during sync operations
     * to preserve the original timestamp from the source.
     *
     * @param timestamp The timestamp to set.
     */
    public void forceUpdatedAt(Instant timestamp) {
        this.updatedAt = timestamp;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        BaseSyncEntity that = (BaseSyncEntity) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
