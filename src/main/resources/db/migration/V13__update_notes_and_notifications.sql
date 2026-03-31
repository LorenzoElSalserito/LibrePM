-- V13: Update Notes (soft delete) and Notifications (refactoring) - SQLite Safe Mode

-- 1. Update Notes table (Recreate to ensure schema consistency)
CREATE TABLE notes_new (
    id VARCHAR(36) PRIMARY KEY,
    title VARCHAR(200),
    content TEXT,
    parent_type VARCHAR(20) NOT NULL,
    parent_id VARCHAR(36) NOT NULL,
    owner_id VARCHAR(36) NOT NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    deleted BOOLEAN NOT NULL DEFAULT 0,
    version BIGINT DEFAULT 0,
    CONSTRAINT fk_note_owner FOREIGN KEY (owner_id) REFERENCES users(id)
);

-- Copy data (if columns exist in source, otherwise defaults)
-- Note: we assume 'deleted' and 'version' might not exist in old table, or might exist if Hibernate touched it.
-- To be safe, we select explicitly. If Hibernate added columns, they are null/default.
-- We use a trick: select only common columns.
INSERT INTO notes_new (id, title, content, parent_type, parent_id, owner_id, created_at, updated_at)
SELECT id, title, content, parent_type, parent_id, owner_id, created_at, updated_at FROM notes;

DROP TABLE notes;
ALTER TABLE notes_new RENAME TO notes;

CREATE INDEX idx_note_parent ON notes(parent_type, parent_id);
CREATE INDEX idx_note_owner ON notes(owner_id);


-- 2. Update Notifications table (Recreate to handle renames and new columns)
CREATE TABLE notifications_new (
    id VARCHAR(36) PRIMARY KEY,
    recipient_id VARCHAR(36) NOT NULL,
    sender_id VARCHAR(36),
    type VARCHAR(50) NOT NULL,
    reference_id VARCHAR(255),
    reference_type VARCHAR(50),
    message VARCHAR(500) NOT NULL,
    is_read BOOLEAN NOT NULL DEFAULT 0,
    created_at TIMESTAMP NOT NULL,
    CONSTRAINT fk_notif_recipient FOREIGN KEY (recipient_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_notif_sender FOREIGN KEY (sender_id) REFERENCES users(id) ON DELETE SET NULL
);

-- Copy data mapping old columns to new ones
-- user_id -> recipient_id
-- entity_id -> reference_id
-- type, message, is_read, created_at -> same
-- sender_id, reference_type -> null (new)
INSERT INTO notifications_new (id, recipient_id, type, reference_id, message, is_read, created_at)
SELECT id, user_id, type, entity_id, message, is_read, created_at FROM notifications;

DROP TABLE notifications;
ALTER TABLE notifications_new RENAME TO notifications;

CREATE INDEX idx_notif_recipient ON notifications(recipient_id);
CREATE INDEX idx_notif_read ON notifications(is_read);
