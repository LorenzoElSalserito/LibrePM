-- V45: Polymorphic asset linking
CREATE TABLE IF NOT EXISTS asset_links (
    id                  VARCHAR(36) PRIMARY KEY,
    asset_id            VARCHAR(36) NOT NULL,
    linked_entity_type  VARCHAR(64) NOT NULL,
    linked_entity_id    VARCHAR(36) NOT NULL,
    created_at          TIMESTAMP   DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (asset_id) REFERENCES assets(id)
);

CREATE INDEX IF NOT EXISTS idx_asset_links_entity ON asset_links(linked_entity_type, linked_entity_id);
CREATE INDEX IF NOT EXISTS idx_asset_links_asset ON asset_links(asset_id);
