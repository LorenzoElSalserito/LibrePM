-- V19: Add missing sync columns to notes
-- Author: Lorenzo DM
-- Date: 2026-02-06

ALTER TABLE notes ADD COLUMN last_synced_at TEXT;
ALTER TABLE notes ADD COLUMN sync_status TEXT DEFAULT 'LOCAL_ONLY';
