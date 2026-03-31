-- V20: Add updated_at column to assets table
-- Author: Lorenzo DM
-- Date: 2025-02-09

ALTER TABLE assets ADD COLUMN updated_at TEXT;

-- Initialize updated_at with created_at for existing records
UPDATE assets SET updated_at = created_at WHERE updated_at IS NULL;
