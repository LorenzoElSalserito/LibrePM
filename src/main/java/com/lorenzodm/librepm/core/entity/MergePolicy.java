package com.lorenzodm.librepm.core.entity;

import jakarta.persistence.*;

/**
 * Defines a semantic merge policy for a specific entity type during sync (PRD-13-FR-003).
 * PRD-13-FR-004: LWW MUST be limited to semantically appropriate cases.
 * PRD-13-BR-001..005: specific rules per entity type.
 *
 * @author Lorenzo DM
 * @since 1.0.0
 * @version 1.0.0
 */
@Entity
@Table(name = "merge_policies", indexes = {
        @Index(name = "idx_mp_entity_type", columnList = "entity_type")
})
public class MergePolicy extends BaseSyncEntity {

    /**
     * Merge strategy for the entity type.
     * PRD-13-FR-003: differentiated per data category.
     */
    public enum PolicyType {
        /** Simple LWW by updatedAt — limited to semantically safe fields (PRD-13-FR-004). */
        LAST_WRITE_WINS,
        /** Requires human decision; creates a ConflictRecord (PRD-13-FR-005). */
        MANUAL,
        /** Set union — used for tags, permissions (PRD-13-BR-003). */
        UNION,
        /** List merge by item identity, preserving ordering (PRD-13-BR-002). */
        LIST_MERGE,
        /** Graph edge merge — used for dependencies (PRD-13-BR-001). */
        GRAPH_MERGE,
        /** Snapshot is immutable; remote updates are ignored (PRD-13-BR-004). */
        IMMUTABLE_SNAPSHOT,
        /** Append-only: new records from both sides are kept (PRD-13-BR-005). */
        APPEND_ONLY
    }

    /** Entity type identifier, e.g. "Task", "Note", "Dependency", "Baseline". */
    @Column(nullable = false, unique = true, length = 100)
    private String entityType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private PolicyType policy;

    /** Human-readable rationale for the policy choice. */
    @Column(columnDefinition = "TEXT")
    private String description;

    /**
     * When true the engine can auto-resolve without creating a ConflictRecord.
     * When false a ConflictRecord is always produced for human review.
     */
    @Column(nullable = false)
    private boolean autoResolvable;

    /**
     * Comma-separated field names this policy applies to.
     * NULL or "*" means the policy covers the entire entity.
     */
    @Column(length = 500)
    private String fieldScope;

    public MergePolicy() {
        super();
    }

    // Getters and Setters
    public String getEntityType() { return entityType; }
    public void setEntityType(String entityType) { this.entityType = entityType; }
    public PolicyType getPolicy() { return policy; }
    public void setPolicy(PolicyType policy) { this.policy = policy; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public boolean isAutoResolvable() { return autoResolvable; }
    public void setAutoResolvable(boolean autoResolvable) { this.autoResolvable = autoResolvable; }
    public String getFieldScope() { return fieldScope; }
    public void setFieldScope(String fieldScope) { this.fieldScope = fieldScope; }
}
