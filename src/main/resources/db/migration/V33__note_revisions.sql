-- V33: Note revisions for lightweight versioning (PRD-02-FR-006)
CREATE TABLE note_revisions (
    id VARCHAR(36) PRIMARY KEY,
    note_id VARCHAR(36) NOT NULL,
    revision_number INTEGER NOT NULL,
    title VARCHAR(255),
    content TEXT,
    author_id VARCHAR(36),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (note_id) REFERENCES notes(id),
    FOREIGN KEY (author_id) REFERENCES users(id)
);
CREATE INDEX idx_note_revisions ON note_revisions(note_id, revision_number);
