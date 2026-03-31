-- ============================================
-- V25: Phase 5 - Planning-Aware Sync (PRD-13)
--              + Integrations/ICS/CSV (PRD-14)
-- ============================================

-- -----------------------------------------------
-- Merge Policies (PRD-13-FR-003, FR-004)
-- One row per entity type; seeded with defaults.
-- -----------------------------------------------
CREATE TABLE merge_policies (
    id TEXT PRIMARY KEY,
    entity_type TEXT NOT NULL UNIQUE,
    policy TEXT NOT NULL DEFAULT 'MANUAL',
    description TEXT,
    auto_resolvable INTEGER NOT NULL DEFAULT 0,
    field_scope TEXT,
    created_at TEXT NOT NULL,
    updated_at TEXT NOT NULL,
    deleted_at TEXT,
    last_synced_at TEXT,
    sync_status TEXT DEFAULT 'LOCAL_ONLY'
);

CREATE INDEX idx_mp_entity_type ON merge_policies(entity_type);

-- -----------------------------------------------
-- Conflict Records (PRD-13-FR-005)
-- Created when auto-resolution is not possible.
-- -----------------------------------------------
CREATE TABLE conflict_records (
    id TEXT PRIMARY KEY,
    entity_type TEXT NOT NULL,
    entity_id TEXT NOT NULL,
    local_state TEXT,
    remote_state TEXT,
    detected_at TEXT NOT NULL,
    resolved INTEGER NOT NULL DEFAULT 0,
    field_name TEXT,
    policy_used TEXT,
    resolution TEXT,
    resolved_at TEXT,
    resolved_by TEXT,
    created_at TEXT NOT NULL,
    updated_at TEXT NOT NULL,
    deleted_at TEXT,
    last_synced_at TEXT,
    sync_status TEXT DEFAULT 'LOCAL_ONLY'
);

CREATE INDEX idx_conflict_entity ON conflict_records(entity_type, entity_id);

-- -----------------------------------------------
-- Audit Events (PRD-13-FR-006)
-- Append-only; never updated or soft-deleted.
-- -----------------------------------------------
CREATE TABLE audit_events (
    id TEXT PRIMARY KEY,
    entity_type TEXT NOT NULL,
    entity_id TEXT NOT NULL,
    action TEXT NOT NULL,
    user_id TEXT,
    event_timestamp TEXT NOT NULL,
    details TEXT,
    created_at TEXT NOT NULL,
    updated_at TEXT NOT NULL,
    deleted_at TEXT,
    last_synced_at TEXT,
    sync_status TEXT DEFAULT 'LOCAL_ONLY'
);

CREATE INDEX idx_audit_entity    ON audit_events(entity_type, entity_id);
CREATE INDEX idx_audit_user      ON audit_events(user_id);
CREATE INDEX idx_audit_ts        ON audit_events(event_timestamp);

-- -----------------------------------------------
-- Calendar Feed Tokens (PRD-14-FR-001, BR-001)
-- One token per user; regenerable.
-- -----------------------------------------------
CREATE TABLE calendar_feed_tokens (
    id TEXT PRIMARY KEY,
    user_id TEXT NOT NULL UNIQUE,
    token TEXT NOT NULL UNIQUE,
    included_entity_types TEXT DEFAULT 'Task',
    description TEXT,
    last_accessed_at TEXT,
    created_at TEXT NOT NULL,
    updated_at TEXT NOT NULL,
    deleted_at TEXT,
    last_synced_at TEXT,
    sync_status TEXT DEFAULT 'LOCAL_ONLY',
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

CREATE INDEX idx_cft_user  ON calendar_feed_tokens(user_id);
CREATE INDEX idx_cft_token ON calendar_feed_tokens(token);

-- -----------------------------------------------
-- Import/Export Jobs (PRD-14-FR-009)
-- Traceable job records for every export/import.
-- -----------------------------------------------
CREATE TABLE import_export_jobs (
    id TEXT PRIMARY KEY,
    user_id TEXT NOT NULL,
    project_id TEXT,
    job_type TEXT NOT NULL,
    status TEXT NOT NULL DEFAULT 'PENDING',
    result_payload TEXT,
    error_message TEXT,
    record_count INTEGER,
    completed_at TEXT,
    created_at TEXT NOT NULL,
    updated_at TEXT NOT NULL,
    deleted_at TEXT,
    last_synced_at TEXT,
    sync_status TEXT DEFAULT 'LOCAL_ONLY'
);

CREATE INDEX idx_job_user    ON import_export_jobs(user_id);
CREATE INDEX idx_job_project ON import_export_jobs(project_id);
CREATE INDEX idx_job_status  ON import_export_jobs(status);

-- -----------------------------------------------
-- Default Merge Policies seed (PRD-13-BR-001..005)
-- One row per entity type with canonical strategy.
-- -----------------------------------------------

-- Task: manual — tasks are complex objects; human review required (PRD-13)
INSERT INTO merge_policies (id, entity_type, policy, description, auto_resolvable, created_at, updated_at)
VALUES (lower(hex(randomblob(16))), 'Task', 'MANUAL',
        'Task conflicts require human review due to complex interdependencies.', 0,
        datetime('now'), datetime('now'));

-- Note: LWW by updatedAt — notes are standalone, LWW is semantically safe
INSERT INTO merge_policies (id, entity_type, policy, description, auto_resolvable, created_at, updated_at)
VALUES (lower(hex(randomblob(16))), 'Note', 'LAST_WRITE_WINS',
        'Notes are standalone; last-write-wins is semantically safe.', 1,
        datetime('now'), datetime('now'));

-- Dependency: graph merge — edges must be merged preserving graph structure (PRD-13-BR-001)
INSERT INTO merge_policies (id, entity_type, policy, description, auto_resolvable, created_at, updated_at)
VALUES (lower(hex(randomblob(16))), 'Dependency', 'GRAPH_MERGE',
        'Dependency edges are merged preserving directed-graph structure. PRD-13-BR-001.', 1,
        datetime('now'), datetime('now'));

-- Assignment: list merge by identity — union of assignments with deduplication (PRD-13-BR-002)
INSERT INTO merge_policies (id, entity_type, policy, description, auto_resolvable, created_at, updated_at)
VALUES (lower(hex(randomblob(16))), 'Assignment', 'LIST_MERGE',
        'Assignments merged by member identity; both sides'' assignments preserved. PRD-13-BR-002.', 1,
        datetime('now'), datetime('now'));

-- PermissionPolicy: union — permissions are additive; never subtractive (PRD-13-BR-003)
INSERT INTO merge_policies (id, entity_type, policy, description, auto_resolvable, created_at, updated_at)
VALUES (lower(hex(randomblob(16))), 'PermissionPolicy', 'UNION',
        'Permission policies are additive (union). PRD-13-BR-003.', 1,
        datetime('now'), datetime('now'));

-- Baseline: immutable snapshot — once taken, a baseline cannot be altered (PRD-13-BR-004)
INSERT INTO merge_policies (id, entity_type, policy, description, auto_resolvable, created_at, updated_at)
VALUES (lower(hex(randomblob(16))), 'Baseline', 'IMMUTABLE_SNAPSHOT',
        'Baselines are immutable snapshots; remote updates are ignored. PRD-13-BR-004.', 1,
        datetime('now'), datetime('now'));

-- AuditEvent: append-only — audit log grows in both directions; all entries kept (PRD-13-BR-005)
INSERT INTO merge_policies (id, entity_type, policy, description, auto_resolvable, created_at, updated_at)
VALUES (lower(hex(randomblob(16))), 'AuditEvent', 'APPEND_ONLY',
        'Audit events are append-only; all events from both sides are kept. PRD-13-BR-005.', 1,
        datetime('now'), datetime('now'));

-- ResourceAllocation: manual — allocation conflicts have financial/scheduling implications
INSERT INTO merge_policies (id, entity_type, policy, description, auto_resolvable, created_at, updated_at)
VALUES (lower(hex(randomblob(16))), 'ResourceAllocation', 'MANUAL',
        'Resource allocation conflicts require human review due to scheduling/financial impact.', 0,
        datetime('now'), datetime('now'));

-- WorkCalendar: manual — calendar changes affect the entire planning engine
INSERT INTO merge_policies (id, entity_type, policy, description, auto_resolvable, created_at, updated_at)
VALUES (lower(hex(randomblob(16))), 'WorkCalendar', 'MANUAL',
        'Work calendar changes affect the planning engine; human review required.', 0,
        datetime('now'), datetime('now'));

-- WbsNode: list merge — WBS structure preserved; nodes merged by position key
INSERT INTO merge_policies (id, entity_type, policy, description, auto_resolvable, created_at, updated_at)
VALUES (lower(hex(randomblob(16))), 'WbsNode', 'LIST_MERGE',
        'WBS nodes merged by outline key preserving hierarchy structure.', 1,
        datetime('now'), datetime('now'));
