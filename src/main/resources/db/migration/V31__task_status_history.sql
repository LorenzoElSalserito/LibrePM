-- V31: Task status history (PRD-01-FR-008)

CREATE TABLE IF NOT EXISTS task_status_history (
    id TEXT PRIMARY KEY,
    task_id TEXT NOT NULL,
    old_status TEXT,
    new_status TEXT NOT NULL,
    changed_by TEXT,
    changed_at TEXT NOT NULL DEFAULT (datetime('now')),
    comment TEXT,
    FOREIGN KEY (task_id) REFERENCES tasks(id) ON DELETE CASCADE,
    FOREIGN KEY (changed_by) REFERENCES users(id) ON DELETE SET NULL
);

CREATE INDEX IF NOT EXISTS idx_task_status_history ON task_status_history(task_id, changed_at);
CREATE INDEX IF NOT EXISTS idx_task_status_history_user ON task_status_history(changed_by);
