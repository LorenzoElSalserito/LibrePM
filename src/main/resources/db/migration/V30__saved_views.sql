-- V30: Saved views and persistent filters (PRD-01-FR-007, PRD-10-FR-003)

CREATE TABLE IF NOT EXISTS saved_views (
    id TEXT PRIMARY KEY,
    name TEXT NOT NULL,
    description TEXT,
    project_id TEXT,
    user_id TEXT NOT NULL,
    view_type TEXT NOT NULL, -- LIST, BOARD, GANTT, CALENDAR, WORKLOAD
    filters_json TEXT,       -- JSON with active filters
    sort_json TEXT,          -- JSON with sort criteria
    grouping_json TEXT,      -- JSON with grouping config
    columns_json TEXT,       -- JSON with visible columns
    is_default BOOLEAN DEFAULT FALSE,
    is_shared BOOLEAN DEFAULT FALSE,
    created_at TEXT NOT NULL DEFAULT (datetime('now')),
    updated_at TEXT NOT NULL DEFAULT (datetime('now')),
    deleted_at TEXT,
    last_synced_at TEXT,
    sync_status TEXT DEFAULT 'LOCAL_ONLY',
    FOREIGN KEY (project_id) REFERENCES projects(id) ON DELETE CASCADE,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

CREATE INDEX IF NOT EXISTS idx_saved_views_user ON saved_views(user_id);
CREATE INDEX IF NOT EXISTS idx_saved_views_project ON saved_views(project_id);
CREATE INDEX IF NOT EXISTS idx_saved_views_type ON saved_views(view_type);
