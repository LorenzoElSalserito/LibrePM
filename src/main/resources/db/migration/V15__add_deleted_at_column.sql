-- V15: Add deleted_at column for soft-delete sync strategy
-- Author: Lorenzo DM
-- Date: 2024-07-26

ALTER TABLE projects ADD COLUMN deleted_at TEXT;
ALTER TABLE tasks ADD COLUMN deleted_at TEXT;

-- Optional: Add to other syncable entities if needed in the future
-- ALTER TABLE users ADD COLUMN deleted_at TEXT;
-- ALTER TABLE notes ADD COLUMN deleted_at TEXT;
-- ALTER TABLE tags ADD COLUMN deleted_at TEXT;
