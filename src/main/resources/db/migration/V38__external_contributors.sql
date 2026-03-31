-- V38: External contributors (PRD-04-FR-003)
CREATE TABLE IF NOT EXISTS external_contributors (
    id TEXT PRIMARY KEY,
    display_name TEXT NOT NULL,
    email TEXT,
    organization TEXT,
    role_id TEXT,
    scope TEXT NOT NULL DEFAULT 'PROJECT',
    scope_entity_id TEXT,
    access_token_hash TEXT,
    access_expires_at TEXT,
    created_at TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted_at TEXT,
    last_synced_at TEXT,
    sync_status TEXT DEFAULT 'LOCAL_ONLY',
    created_by TEXT,
    FOREIGN KEY (role_id) REFERENCES roles(id),
    FOREIGN KEY (created_by) REFERENCES users(id)
);

CREATE INDEX IF NOT EXISTS idx_ext_contrib_scope ON external_contributors(scope, scope_entity_id);
CREATE INDEX IF NOT EXISTS idx_ext_contrib_created_by ON external_contributors(created_by);
