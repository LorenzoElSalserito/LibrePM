-- ============================================
-- V26: Add category column to task_statuses
-- Maps each status to a semantic category (TODO, IN_PROGRESS, DONE, BLOCKED, ARCHIVED)
-- so business logic can operate on categories regardless of user-defined names.
-- ============================================

ALTER TABLE task_statuses ADD COLUMN category TEXT NOT NULL DEFAULT 'TODO';

-- Populate category based on existing status names
UPDATE task_statuses SET category = 'TODO' WHERE name = 'TODO';
UPDATE task_statuses SET category = 'IN_PROGRESS' WHERE name IN ('IN_PROGRESS', 'REVIEW');
UPDATE task_statuses SET category = 'DONE' WHERE name IN ('DONE', 'COMPLETED');
UPDATE task_statuses SET category = 'BLOCKED' WHERE name = 'BLOCKED';
UPDATE task_statuses SET category = 'ARCHIVED' WHERE name IN ('CANCELLED', 'ARCHIVED');
