-- V53: Import mapping profiles for semantic import/export
CREATE TABLE IF NOT EXISTS import_mapping_profiles (
    id          VARCHAR(36)  PRIMARY KEY,
    name        VARCHAR(128) NOT NULL,
    entity_type VARCHAR(64)  NOT NULL,
    mapping_json TEXT         NOT NULL,
    created_by  VARCHAR(36),
    created_at  TIMESTAMP    DEFAULT CURRENT_TIMESTAMP
);
