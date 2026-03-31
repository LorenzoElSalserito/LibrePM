-- ============================================
-- V23: Phase 3 - Executive Dashboard, Baselines,
-- OKRs, Deliverables, Resource Planning
-- PRD-11, PRD-12, PRD-17
-- ============================================

-- ============================================
-- Project Charters (PRD-17-FR-002, PRD-17-FR-003)
-- ============================================
CREATE TABLE project_charters (
    id TEXT PRIMARY KEY,
    project_id TEXT NOT NULL UNIQUE,
    sponsor TEXT,
    project_manager TEXT,
    objectives TEXT,
    problem_statement TEXT,
    business_case TEXT,
    created_at TEXT NOT NULL,
    updated_at TEXT NOT NULL,
    deleted_at TEXT,
    last_synced_at TEXT,
    sync_status TEXT DEFAULT 'LOCAL_ONLY',
    FOREIGN KEY (project_id) REFERENCES projects(id) ON DELETE CASCADE
);

-- ============================================
-- Deliverables (PRD-17-FR-005: key deliverables panel)
-- ============================================
CREATE TABLE deliverables (
    id TEXT PRIMARY KEY,
    project_id TEXT NOT NULL,
    name TEXT NOT NULL,
    description TEXT,
    due_date TEXT,
    progress INTEGER NOT NULL DEFAULT 0,
    risk_status TEXT NOT NULL DEFAULT 'OK',
    completed_at TEXT,
    created_at TEXT NOT NULL,
    updated_at TEXT NOT NULL,
    deleted_at TEXT,
    last_synced_at TEXT,
    sync_status TEXT DEFAULT 'LOCAL_ONLY',
    FOREIGN KEY (project_id) REFERENCES projects(id) ON DELETE CASCADE
);

CREATE INDEX idx_deliverable_project ON deliverables(project_id);

-- ============================================
-- Risk Register Entries (PRD-17-FR-006)
-- ============================================
CREATE TABLE risk_register_entries (
    id TEXT PRIMARY KEY,
    project_id TEXT NOT NULL,
    description TEXT NOT NULL,
    probability TEXT NOT NULL,
    impact TEXT NOT NULL,
    mitigation_strategy TEXT,
    created_at TEXT NOT NULL,
    updated_at TEXT NOT NULL,
    deleted_at TEXT,
    last_synced_at TEXT,
    sync_status TEXT DEFAULT 'LOCAL_ONLY',
    FOREIGN KEY (project_id) REFERENCES projects(id) ON DELETE CASCADE
);

CREATE INDEX idx_risk_project ON risk_register_entries(project_id);

-- ============================================
-- OKRs (PRD-17-FR-007)
-- ============================================
CREATE TABLE okrs (
    id TEXT PRIMARY KEY,
    project_id TEXT NOT NULL,
    objective TEXT NOT NULL,
    created_at TEXT NOT NULL,
    updated_at TEXT NOT NULL,
    deleted_at TEXT,
    last_synced_at TEXT,
    sync_status TEXT DEFAULT 'LOCAL_ONLY',
    FOREIGN KEY (project_id) REFERENCES projects(id) ON DELETE CASCADE
);

CREATE INDEX idx_okr_project ON okrs(project_id);

-- ============================================
-- Success Metrics (Key Results for OKRs)
-- ============================================
CREATE TABLE success_metrics (
    id TEXT PRIMARY KEY,
    okr_id TEXT NOT NULL,
    name TEXT NOT NULL,
    target_value REAL NOT NULL DEFAULT 0,
    current_value REAL NOT NULL DEFAULT 0,
    unit TEXT,
    created_at TEXT NOT NULL,
    updated_at TEXT NOT NULL,
    deleted_at TEXT,
    last_synced_at TEXT,
    sync_status TEXT DEFAULT 'LOCAL_ONLY',
    FOREIGN KEY (okr_id) REFERENCES okrs(id) ON DELETE CASCADE
);

CREATE INDEX idx_successmetric_okr ON success_metrics(okr_id);

-- ============================================
-- Target Achieved Records (historical achievement tracking)
-- ============================================
CREATE TABLE target_achieved_records (
    id TEXT PRIMARY KEY,
    metric_id TEXT NOT NULL,
    achieved_value REAL NOT NULL,
    record_date TEXT NOT NULL,
    note TEXT,
    created_at TEXT NOT NULL,
    updated_at TEXT NOT NULL,
    deleted_at TEXT,
    last_synced_at TEXT,
    sync_status TEXT DEFAULT 'LOCAL_ONLY',
    FOREIGN KEY (metric_id) REFERENCES success_metrics(id) ON DELETE CASCADE
);

CREATE INDEX idx_targetrecord_metric ON target_achieved_records(metric_id);
CREATE INDEX idx_targetrecord_date ON target_achieved_records(record_date);

-- ============================================
-- Baselines (PRD-11-FR-001: immutable plan snapshots)
-- ============================================
CREATE TABLE baselines (
    id TEXT PRIMARY KEY,
    project_id TEXT NOT NULL,
    name TEXT NOT NULL,
    snapshot_date TEXT NOT NULL,
    created_at TEXT NOT NULL,
    updated_at TEXT NOT NULL,
    deleted_at TEXT,
    last_synced_at TEXT,
    sync_status TEXT DEFAULT 'LOCAL_ONLY',
    FOREIGN KEY (project_id) REFERENCES projects(id) ON DELETE CASCADE
);

CREATE INDEX idx_baseline_project ON baselines(project_id);

-- ============================================
-- Baseline Task Snapshots (PRD-11-FR-001)
-- ============================================
CREATE TABLE baseline_task_snapshots (
    id TEXT PRIMARY KEY,
    baseline_id TEXT NOT NULL,
    task_id TEXT NOT NULL,
    planned_start TEXT,
    planned_finish TEXT,
    estimated_effort INTEGER,
    created_at TEXT NOT NULL,
    updated_at TEXT NOT NULL,
    deleted_at TEXT,
    last_synced_at TEXT,
    sync_status TEXT DEFAULT 'LOCAL_ONLY',
    FOREIGN KEY (baseline_id) REFERENCES baselines(id) ON DELETE CASCADE,
    FOREIGN KEY (task_id) REFERENCES tasks(id) ON DELETE CASCADE
);

CREATE INDEX idx_snapshot_baseline ON baseline_task_snapshots(baseline_id);
CREATE INDEX idx_snapshot_task ON baseline_task_snapshots(task_id);

-- ============================================
-- Resource Allocations (PRD-12-FR-001)
-- ============================================
CREATE TABLE resource_allocations (
    id TEXT PRIMARY KEY,
    user_id TEXT NOT NULL,
    project_id TEXT,
    start_date TEXT NOT NULL,
    end_date TEXT NOT NULL,
    percentage INTEGER NOT NULL DEFAULT 100,
    created_at TEXT NOT NULL,
    updated_at TEXT NOT NULL,
    deleted_at TEXT,
    last_synced_at TEXT,
    sync_status TEXT DEFAULT 'LOCAL_ONLY',
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (project_id) REFERENCES projects(id) ON DELETE SET NULL
);

CREATE INDEX idx_allocation_user ON resource_allocations(user_id);
CREATE INDEX idx_allocation_project ON resource_allocations(project_id);
CREATE INDEX idx_allocation_period ON resource_allocations(start_date, end_date);

-- ============================================
-- ALTER assignments: add estimated_effort column (PRD-12-FR-002)
-- ============================================
ALTER TABLE assignments ADD COLUMN estimated_effort INTEGER;

-- ============================================
-- End V23
-- ============================================
