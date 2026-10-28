-- Attachments travel with the shared database: bytes live in asset_contents
-- (512 KiB chunks, LONGBLOB) so every client can serve files uploaded elsewhere.
-- Idempotent: databases provisioned from a recent SQLite schema already have both.
ALTER TABLE assets ADD COLUMN IF NOT EXISTS project_id VARCHAR(191) NULL;

UPDATE assets a JOIN tasks t ON t.id = a.task_id
SET a.project_id = t.project_id
WHERE a.task_id IS NOT NULL AND a.project_id IS NULL;

CREATE INDEX IF NOT EXISTS idx_assets_project_id ON assets (project_id);

CREATE TABLE IF NOT EXISTS asset_contents (
    asset_id VARCHAR(191) NOT NULL,
    chunk_index BIGINT NOT NULL,
    chunk_size BIGINT NOT NULL,
    data LONGBLOB NOT NULL,
    PRIMARY KEY (asset_id, chunk_index),
    CONSTRAINT fk_asset_contents_asset_id FOREIGN KEY (asset_id) REFERENCES assets (id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
