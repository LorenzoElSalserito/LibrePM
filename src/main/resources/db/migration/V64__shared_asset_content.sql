-- V64: attachments travel with the database.
-- File bytes are stored in asset_contents (512 KiB chunks) so every backend sharing the
-- database can serve them; the local assets folder stays as a cache. project_id makes
-- membership checks and project file lists cheap for task and project attachments.

ALTER TABLE assets ADD COLUMN project_id VARCHAR(36);

UPDATE assets
SET project_id = (SELECT t.project_id FROM tasks t WHERE t.id = assets.task_id)
WHERE task_id IS NOT NULL AND project_id IS NULL;

CREATE INDEX idx_asset_project ON assets(project_id);

CREATE TABLE asset_contents (
    asset_id VARCHAR(36) NOT NULL,
    chunk_index INTEGER NOT NULL,
    chunk_size INTEGER NOT NULL,
    data BLOB NOT NULL,
    PRIMARY KEY (asset_id, chunk_index),
    FOREIGN KEY (asset_id) REFERENCES assets(id) ON DELETE CASCADE
);
