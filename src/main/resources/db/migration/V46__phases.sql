-- V46: Project phases
CREATE TABLE IF NOT EXISTS phases (
    id            VARCHAR(36)  PRIMARY KEY,
    project_id    VARCHAR(36)  NOT NULL,
    name          VARCHAR(255) NOT NULL,
    description   TEXT,
    sort_order    INTEGER      DEFAULT 0,
    planned_start DATE,
    planned_end   DATE,
    actual_start  DATE,
    actual_end    DATE,
    status        VARCHAR(32)  DEFAULT 'NOT_STARTED',
    color         VARCHAR(7),
    created_at    TIMESTAMP    DEFAULT CURRENT_TIMESTAMP,
    deleted_at    TIMESTAMP,
    FOREIGN KEY (project_id) REFERENCES projects(id)
);

CREATE INDEX IF NOT EXISTS idx_phases_project ON phases(project_id);

-- Note: phase_id column was already added to tasks table in V29__task_extended_fields.sql
-- In V29 it referenced wbs_nodes, we might want to change it later if needed, but for now we skip adding it again to avoid duplication errors.
