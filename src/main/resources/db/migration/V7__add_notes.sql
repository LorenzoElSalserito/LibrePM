-- V7: Add Notes table

CREATE TABLE notes (
    id VARCHAR(36) PRIMARY KEY,
    title VARCHAR(200),
    content TEXT,
    parent_type VARCHAR(20) NOT NULL,
    parent_id VARCHAR(36) NOT NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    owner_id VARCHAR(36) NOT NULL,
    CONSTRAINT fk_note_owner FOREIGN KEY (owner_id) REFERENCES users(id) ON DELETE CASCADE
);

CREATE TABLE note_tags (
    note_id VARCHAR(36) NOT NULL,
    tag_id VARCHAR(36) NOT NULL,
    PRIMARY KEY (note_id, tag_id),
    CONSTRAINT fk_notetag_note FOREIGN KEY (note_id) REFERENCES notes(id) ON DELETE CASCADE,
    CONSTRAINT fk_notetag_tag FOREIGN KEY (tag_id) REFERENCES tags(id) ON DELETE CASCADE
);

CREATE INDEX idx_note_parent ON notes(parent_type, parent_id);
CREATE INDEX idx_note_owner ON notes(owner_id);
