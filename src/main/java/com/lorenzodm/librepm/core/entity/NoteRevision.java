package com.lorenzodm.librepm.core.entity;

import jakarta.persistence.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "note_revisions", indexes = {
        @Index(name = "idx_note_revisions", columnList = "note_id, revision_number")
})
public class NoteRevision {

    @Id
    @Column(length = 36)
    private String id;

    @Column(name = "note_id", nullable = false, length = 36)
    private String noteId;

    @Column(name = "revision_number", nullable = false)
    private int revisionNumber;

    @Column(length = 255)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String content;

    @Column(name = "author_id", length = 36)
    private String authorId;

    @Column(name = "created_at")
    private Instant createdAt;

    public NoteRevision() {
        this.id = UUID.randomUUID().toString();
        this.createdAt = Instant.now();
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getNoteId() { return noteId; }
    public void setNoteId(String noteId) { this.noteId = noteId; }

    public int getRevisionNumber() { return revisionNumber; }
    public void setRevisionNumber(int revisionNumber) { this.revisionNumber = revisionNumber; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public String getAuthorId() { return authorId; }
    public void setAuthorId(String authorId) { this.authorId = authorId; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
}
