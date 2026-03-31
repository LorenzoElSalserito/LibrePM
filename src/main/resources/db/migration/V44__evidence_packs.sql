-- V44: Evidence packs and asset evidence metadata
ALTER TABLE assets ADD COLUMN evidence_type VARCHAR(32);
ALTER TABLE assets ADD COLUMN is_frozen BOOLEAN DEFAULT FALSE;
ALTER TABLE assets ADD COLUMN frozen_at TIMESTAMP;

CREATE TABLE IF NOT EXISTS evidence_packs (
    id            VARCHAR(36)  PRIMARY KEY,
    name          VARCHAR(255) NOT NULL,
    description   TEXT,
    project_id    VARCHAR(36),
    pack_type     VARCHAR(32),
    status        VARCHAR(32)  DEFAULT 'DRAFT',
    created_by    VARCHAR(36),
    created_at    TIMESTAMP    DEFAULT CURRENT_TIMESTAMP,
    finalized_at  TIMESTAMP,
    deleted_at    TIMESTAMP,
    FOREIGN KEY (project_id) REFERENCES projects(id),
    FOREIGN KEY (created_by) REFERENCES users(id)
);

CREATE TABLE IF NOT EXISTS evidence_pack_items (
    id          VARCHAR(36) PRIMARY KEY,
    pack_id     VARCHAR(36) NOT NULL,
    asset_id    VARCHAR(36),
    note_id     VARCHAR(36),
    item_order  INTEGER     DEFAULT 0,
    FOREIGN KEY (pack_id) REFERENCES evidence_packs(id),
    FOREIGN KEY (asset_id) REFERENCES assets(id),
    FOREIGN KEY (note_id) REFERENCES notes(id)
);
