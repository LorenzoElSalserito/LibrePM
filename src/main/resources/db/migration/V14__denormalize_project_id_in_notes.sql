-- V14: Denormalize project_id in notes for efficient feed queries

-- 1. Add project_id column
ALTER TABLE notes ADD COLUMN project_id VARCHAR(36);

-- 2. Backfill project_id for TASK notes
-- SQLite syntax for update with join/subquery
UPDATE notes
SET project_id = (
    SELECT t.project_id
    FROM tasks t
    WHERE t.id = notes.parent_id
)
WHERE parent_type = 'TASK';

-- 3. Backfill project_id for PROJECT notes (parent_id is already project_id)
UPDATE notes
SET project_id = parent_id
WHERE parent_type = 'PROJECT';

-- 4. Create index for performance
CREATE INDEX idx_note_project_updated ON notes(project_id, updated_at);
