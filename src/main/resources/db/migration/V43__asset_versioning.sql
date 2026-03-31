-- V43: Asset versioning
CREATE TABLE IF NOT EXISTS asset_versions (
    id              VARCHAR(36)  PRIMARY KEY,
    asset_id        VARCHAR(36)  NOT NULL,
    version_number  INTEGER      NOT NULL,
    file_path       VARCHAR(512) NOT NULL,
    file_size       BIGINT,
    checksum        VARCHAR(128),
    uploaded_by     VARCHAR(36),
    uploaded_at     TIMESTAMP    DEFAULT CURRENT_TIMESTAMP,
    comment         TEXT,
    FOREIGN KEY (asset_id) REFERENCES assets(id),
    FOREIGN KEY (uploaded_by) REFERENCES users(id)
);

CREATE INDEX IF NOT EXISTS idx_asset_versions_asset ON asset_versions(asset_id);
