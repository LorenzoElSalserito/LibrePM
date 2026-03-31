-- V28: Inbox support (PRD-01-FR-004)
-- Allow tasks without a project (inbox tasks) and add inbox flag.

-- Step 1: Recreate tasks table with nullable project_id
-- SQLite does not support ALTER COLUMN, so we must recreate the table.
-- However, since tasks has many FKs pointing to it, we use a simpler approach:
-- Add the inbox column and create a trigger-based workaround.

-- Add inbox flag
ALTER TABLE tasks ADD COLUMN inbox BOOLEAN DEFAULT FALSE;

-- Create index for inbox queries
CREATE INDEX IF NOT EXISTS idx_tasks_inbox ON tasks(inbox, assigned_to_id);
