-- V17: Add version column for Optimistic Locking
-- Author: Lorenzo DM
-- Date: 2026-01-30

ALTER TABLE tasks ADD COLUMN version BIGINT DEFAULT 0;
ALTER TABLE projects ADD COLUMN version BIGINT DEFAULT 0;
