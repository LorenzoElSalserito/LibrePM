package com.lorenzodm.librepm.core.entity;

import jakarta.persistence.*;

/**
 * Represents a polymorphic link between a Note and any other entity in the system.
 * This allows a single note to be associated with multiple contexts (e.g., a Task, a Project, a Deliverable).
 *
 * @author Lorenzo DM
 * @since 1.0.0
 * @version 1.0.0
 */
@Entity
@Table(name = "note_links", indexes = {
    @Index(name = "idx_notelink_note", columnList = "note_id"),
    @Index(name = "idx_notelink_linked_entity", columnList = "linked_entity_type, linked_entity_id")
})
public class NoteLink extends BaseSyncEntity {

    /**
     * The type of entity this link points to.
     * As per PRD-02, this enables connections to various domain objects.
     */
    public enum LinkedEntityType {
        TASK,
        PROJECT,
        USER,
        TEAM,
        DELIVERABLE,
        OKR,
        RISK
    }

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "note_id", nullable = false)
    private Note note;

    @Enumerated(EnumType.STRING)
    @Column(name = "linked_entity_type", nullable = false, length = 50)
    private LinkedEntityType linkedEntityType;

    @Column(name = "linked_entity_id", nullable = false, length = 36)
    private String linkedEntityId;

    public NoteLink() {
        super();
    }

    // Getters and Setters
    public Note getNote() { return note; }
    public void setNote(Note note) { this.note = note; }
    public LinkedEntityType getLinkedEntityType() { return linkedEntityType; }
    public void setLinkedEntityType(LinkedEntityType linkedEntityType) { this.linkedEntityType = linkedEntityType; }
    public String getLinkedEntityId() { return linkedEntityId; }
    public void setLinkedEntityId(String linkedEntityId) { this.linkedEntityId = linkedEntityId; }
}
