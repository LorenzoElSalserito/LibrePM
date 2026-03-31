-- V40: Approval workflow (PRD-04-FR-007)
CREATE TABLE IF NOT EXISTS approval_requests (
    id TEXT PRIMARY KEY,
    entity_type TEXT NOT NULL,
    entity_id TEXT NOT NULL,
    project_id TEXT,
    requested_by TEXT NOT NULL,
    approver_id TEXT NOT NULL,
    status TEXT DEFAULT 'PENDING',
    comment TEXT,
    requested_at TEXT DEFAULT CURRENT_TIMESTAMP,
    resolved_at TEXT,
    created_at TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted_at TEXT,
    last_synced_at TEXT,
    sync_status TEXT DEFAULT 'LOCAL_ONLY',
    FOREIGN KEY (requested_by) REFERENCES users(id),
    FOREIGN KEY (approver_id) REFERENCES users(id),
    FOREIGN KEY (project_id) REFERENCES projects(id)
);

CREATE INDEX IF NOT EXISTS idx_approval_approver_status ON approval_requests(approver_id, status);
CREATE INDEX IF NOT EXISTS idx_approval_project ON approval_requests(project_id);
