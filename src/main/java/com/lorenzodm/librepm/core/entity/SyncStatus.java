package com.lorenzodm.librepm.core.entity;

/**
 * Represents the synchronization status of an entity.
 * This enum provides a type-safe way to manage sync state, as per PRD-13.
 *
 * @author Lorenzo DM
 * @since 1.0.0
 * @version 1.0.0
 */
public enum SyncStatus {
    /**
     * The entity has been created locally and has not yet been sent to the server.
     */
    LOCAL_ONLY,

    /**
     * The entity is in the process of being synchronized.
     */
    PENDING_SYNC,

    /**
     * The entity is in sync with the server.
     */
    SYNCED,

    /**
     * A conflict occurred during synchronization that requires resolution.
     */
    CONFLICT,

    /**
     * The entity has been logically deleted and is pending deletion on the server.
     */
    SOFT_DELETED
}
