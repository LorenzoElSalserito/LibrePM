-- V16: Add deleted_at column to remaining syncable entities
-- Author: Lorenzo DM
-- Date: 2024-07-26

-- Users
ALTER TABLE users ADD COLUMN deleted_at TEXT;

-- Notes
ALTER TABLE notes ADD COLUMN deleted_at TEXT;

-- Tags
ALTER TABLE tags ADD COLUMN deleted_at TEXT;
ALTER TABLE tags ADD COLUMN updated_at TEXT; -- Tag mancava anche di updated_at
ALTER TABLE tags ADD COLUMN last_synced_at TEXT;
ALTER TABLE tags ADD COLUMN sync_status TEXT DEFAULT 'LOCAL_ONLY';

-- Assets
ALTER TABLE assets ADD COLUMN deleted_at TEXT;
-- Assets aveva già 'deleted' (boolean), ora usiamo deleted_at.
-- Possiamo migrare i dati se necessario, ma per ora aggiungiamo la colonna.
-- UPDATE assets SET deleted_at = datetime('now') WHERE deleted = 1;
