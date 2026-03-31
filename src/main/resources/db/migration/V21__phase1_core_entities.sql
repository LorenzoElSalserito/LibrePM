-- ============================================
-- V21: Phase 1 - Core entities for Work Management,
-- Time Tracking, Team Collaboration and Notes
-- ============================================

-- Task Statuses (configurable workflow states)
CREATE TABLE task_statuses (
    id TEXT PRIMARY KEY,
    name TEXT NOT NULL UNIQUE,
    description TEXT,
    color TEXT,
    created_at TEXT NOT NULL,
    updated_at TEXT NOT NULL,
    deleted_at TEXT,
    last_synced_at TEXT,
    sync_status TEXT DEFAULT 'LOCAL_ONLY'
);

-- Task Priorities (configurable priority levels)
CREATE TABLE task_priorities (
    id TEXT PRIMARY KEY,
    name TEXT NOT NULL UNIQUE,
    level INTEGER NOT NULL DEFAULT 0,
    color TEXT,
    created_at TEXT NOT NULL,
    updated_at TEXT NOT NULL,
    deleted_at TEXT,
    last_synced_at TEXT,
    sync_status TEXT DEFAULT 'LOCAL_ONLY'
);

-- Roles for RBAC
CREATE TABLE roles (
    id TEXT PRIMARY KEY,
    name TEXT NOT NULL UNIQUE,
    description TEXT,
    created_at TEXT NOT NULL,
    updated_at TEXT NOT NULL,
    deleted_at TEXT,
    last_synced_at TEXT,
    sync_status TEXT DEFAULT 'LOCAL_ONLY'
);

-- Permission Policies linked to Roles
CREATE TABLE permission_policies (
    id TEXT PRIMARY KEY,
    role_id TEXT NOT NULL,
    permission TEXT NOT NULL,
    created_at TEXT NOT NULL,
    updated_at TEXT NOT NULL,
    deleted_at TEXT,
    last_synced_at TEXT,
    sync_status TEXT DEFAULT 'LOCAL_ONLY',
    FOREIGN KEY (role_id) REFERENCES roles(id) ON DELETE CASCADE,
    UNIQUE (role_id, permission)
);

CREATE INDEX idx_permission_policies_role ON permission_policies(role_id);

-- Assignments (links users to tasks with roles)
CREATE TABLE assignments (
    id TEXT PRIMARY KEY,
    task_id TEXT NOT NULL,
    user_id TEXT NOT NULL,
    role_id TEXT,
    assigned_at TEXT NOT NULL,
    created_at TEXT NOT NULL,
    updated_at TEXT NOT NULL,
    deleted_at TEXT,
    last_synced_at TEXT,
    sync_status TEXT DEFAULT 'LOCAL_ONLY',
    FOREIGN KEY (task_id) REFERENCES tasks(id) ON DELETE CASCADE,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (role_id) REFERENCES roles(id) ON DELETE SET NULL
);

CREATE INDEX idx_assignments_task ON assignments(task_id);
CREATE INDEX idx_assignments_user ON assignments(user_id);

-- Note Links (polymorphic connections between notes and other entities)
CREATE TABLE note_links (
    id TEXT PRIMARY KEY,
    note_id TEXT NOT NULL,
    linked_entity_type TEXT NOT NULL,
    linked_entity_id TEXT NOT NULL,
    created_at TEXT NOT NULL,
    updated_at TEXT NOT NULL,
    deleted_at TEXT,
    last_synced_at TEXT,
    sync_status TEXT DEFAULT 'LOCAL_ONLY',
    FOREIGN KEY (note_id) REFERENCES notes(id) ON DELETE CASCADE
);

CREATE INDEX idx_note_links_note ON note_links(note_id);
CREATE INDEX idx_note_links_entity ON note_links(linked_entity_type, linked_entity_id);

-- Time Entries (consolidated time logs)
CREATE TABLE time_entries (
    id TEXT PRIMARY KEY,
    task_id TEXT NOT NULL,
    user_id TEXT,
    entry_date TEXT NOT NULL,
    duration_minutes INTEGER NOT NULL,
    type TEXT NOT NULL DEFAULT 'MANUAL',
    description TEXT,
    created_at TEXT NOT NULL,
    updated_at TEXT NOT NULL,
    deleted_at TEXT,
    last_synced_at TEXT,
    sync_status TEXT DEFAULT 'LOCAL_ONLY',
    FOREIGN KEY (task_id) REFERENCES tasks(id) ON DELETE CASCADE,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE SET NULL
);

CREATE INDEX idx_time_entries_task ON time_entries(task_id);
CREATE INDEX idx_time_entries_user ON time_entries(user_id);
CREATE INDEX idx_time_entries_date ON time_entries(entry_date);

-- Effort Estimates (separate estimate from actual)
CREATE TABLE effort_estimates (
    id TEXT PRIMARY KEY,
    task_id TEXT NOT NULL,
    estimator_id TEXT,
    estimated_minutes INTEGER NOT NULL,
    estimation_date TEXT NOT NULL,
    rationale TEXT,
    created_at TEXT NOT NULL,
    updated_at TEXT NOT NULL,
    deleted_at TEXT,
    last_synced_at TEXT,
    sync_status TEXT DEFAULT 'LOCAL_ONLY',
    FOREIGN KEY (task_id) REFERENCES tasks(id) ON DELETE CASCADE,
    FOREIGN KEY (estimator_id) REFERENCES users(id) ON DELETE SET NULL
);

CREATE INDEX idx_effort_estimates_task ON effort_estimates(task_id);
CREATE INDEX idx_effort_estimates_date ON effort_estimates(estimation_date);

-- ============================================
-- Migrate tasks: add FK columns for status and priority entities
-- ============================================

-- Add foreign key columns for new entity-based status and priority
ALTER TABLE tasks ADD COLUMN status_id TEXT REFERENCES task_statuses(id) ON DELETE SET NULL;
ALTER TABLE tasks ADD COLUMN priority_id TEXT REFERENCES task_priorities(id) ON DELETE SET NULL;

-- Add planned start/finish columns (if not already present from earlier migrations)
-- Note: scheduled_start/end were added in V11, but entity uses planned_start/planned_finish
ALTER TABLE tasks ADD COLUMN planned_start TEXT;
ALTER TABLE tasks ADD COLUMN planned_finish TEXT;

-- Add estimated_effort and actual_effort columns (entity field names)
-- Note: estimated_minutes and actual_minutes were added in V5, these are the new named fields
ALTER TABLE tasks ADD COLUMN estimated_effort INTEGER;
ALTER TABLE tasks ADD COLUMN actual_effort INTEGER DEFAULT 0;

CREATE INDEX idx_task_status_id ON tasks(status_id);
CREATE INDEX idx_task_priority_id ON tasks(priority_id);

-- ============================================
-- Seed default Task Statuses
-- ============================================
INSERT INTO task_statuses (id, name, description, color, created_at, updated_at, sync_status)
VALUES
    ('ts-todo', 'TODO', 'Task da fare', '#6B7280', datetime('now'), datetime('now'), 'LOCAL_ONLY'),
    ('ts-in-progress', 'IN_PROGRESS', 'Task in corso', '#3B82F6', datetime('now'), datetime('now'), 'LOCAL_ONLY'),
    ('ts-review', 'REVIEW', 'Task in revisione', '#F59E0B', datetime('now'), datetime('now'), 'LOCAL_ONLY'),
    ('ts-done', 'DONE', 'Task completato', '#10B981', datetime('now'), datetime('now'), 'LOCAL_ONLY'),
    ('ts-blocked', 'BLOCKED', 'Task bloccato', '#EF4444', datetime('now'), datetime('now'), 'LOCAL_ONLY'),
    ('ts-cancelled', 'CANCELLED', 'Task annullato', '#9CA3AF', datetime('now'), datetime('now'), 'LOCAL_ONLY');

-- ============================================
-- Seed default Task Priorities
-- ============================================
INSERT INTO task_priorities (id, name, level, color, created_at, updated_at, sync_status)
VALUES
    ('tp-low', 'LOW', 1, '#6B7280', datetime('now'), datetime('now'), 'LOCAL_ONLY'),
    ('tp-medium', 'MEDIUM', 2, '#3B82F6', datetime('now'), datetime('now'), 'LOCAL_ONLY'),
    ('tp-high', 'HIGH', 3, '#F59E0B', datetime('now'), datetime('now'), 'LOCAL_ONLY'),
    ('tp-critical', 'CRITICAL', 4, '#EF4444', datetime('now'), datetime('now'), 'LOCAL_ONLY');

-- ============================================
-- Seed default Roles
-- ============================================
INSERT INTO roles (id, name, description, created_at, updated_at, sync_status)
VALUES
    ('role-owner', 'OWNER', 'Proprietario del progetto con accesso completo', datetime('now'), datetime('now'), 'LOCAL_ONLY'),
    ('role-admin', 'ADMIN', 'Amministratore con accesso quasi completo', datetime('now'), datetime('now'), 'LOCAL_ONLY'),
    ('role-editor', 'EDITOR', 'Può modificare task, note e deliverable', datetime('now'), datetime('now'), 'LOCAL_ONLY'),
    ('role-viewer', 'VIEWER', 'Accesso in sola lettura', datetime('now'), datetime('now'), 'LOCAL_ONLY');

-- ============================================
-- Seed default Permission Policies
-- ============================================

-- OWNER permissions (all)
INSERT INTO permission_policies (id, role_id, permission, created_at, updated_at, sync_status)
VALUES
    ('pp-owner-1', 'role-owner', 'project:manage', datetime('now'), datetime('now'), 'LOCAL_ONLY'),
    ('pp-owner-2', 'role-owner', 'task:create', datetime('now'), datetime('now'), 'LOCAL_ONLY'),
    ('pp-owner-3', 'role-owner', 'task:edit', datetime('now'), datetime('now'), 'LOCAL_ONLY'),
    ('pp-owner-4', 'role-owner', 'task:delete', datetime('now'), datetime('now'), 'LOCAL_ONLY'),
    ('pp-owner-5', 'role-owner', 'task:assign', datetime('now'), datetime('now'), 'LOCAL_ONLY'),
    ('pp-owner-6', 'role-owner', 'note:create', datetime('now'), datetime('now'), 'LOCAL_ONLY'),
    ('pp-owner-7', 'role-owner', 'note:edit', datetime('now'), datetime('now'), 'LOCAL_ONLY'),
    ('pp-owner-8', 'role-owner', 'note:delete', datetime('now'), datetime('now'), 'LOCAL_ONLY'),
    ('pp-owner-9', 'role-owner', 'member:manage', datetime('now'), datetime('now'), 'LOCAL_ONLY'),
    ('pp-owner-10', 'role-owner', 'planning:manage', datetime('now'), datetime('now'), 'LOCAL_ONLY');

-- ADMIN permissions
INSERT INTO permission_policies (id, role_id, permission, created_at, updated_at, sync_status)
VALUES
    ('pp-admin-1', 'role-admin', 'task:create', datetime('now'), datetime('now'), 'LOCAL_ONLY'),
    ('pp-admin-2', 'role-admin', 'task:edit', datetime('now'), datetime('now'), 'LOCAL_ONLY'),
    ('pp-admin-3', 'role-admin', 'task:delete', datetime('now'), datetime('now'), 'LOCAL_ONLY'),
    ('pp-admin-4', 'role-admin', 'task:assign', datetime('now'), datetime('now'), 'LOCAL_ONLY'),
    ('pp-admin-5', 'role-admin', 'note:create', datetime('now'), datetime('now'), 'LOCAL_ONLY'),
    ('pp-admin-6', 'role-admin', 'note:edit', datetime('now'), datetime('now'), 'LOCAL_ONLY'),
    ('pp-admin-7', 'role-admin', 'note:delete', datetime('now'), datetime('now'), 'LOCAL_ONLY'),
    ('pp-admin-8', 'role-admin', 'member:manage', datetime('now'), datetime('now'), 'LOCAL_ONLY'),
    ('pp-admin-9', 'role-admin', 'planning:manage', datetime('now'), datetime('now'), 'LOCAL_ONLY');

-- EDITOR permissions
INSERT INTO permission_policies (id, role_id, permission, created_at, updated_at, sync_status)
VALUES
    ('pp-editor-1', 'role-editor', 'task:create', datetime('now'), datetime('now'), 'LOCAL_ONLY'),
    ('pp-editor-2', 'role-editor', 'task:edit', datetime('now'), datetime('now'), 'LOCAL_ONLY'),
    ('pp-editor-3', 'role-editor', 'note:create', datetime('now'), datetime('now'), 'LOCAL_ONLY'),
    ('pp-editor-4', 'role-editor', 'note:edit', datetime('now'), datetime('now'), 'LOCAL_ONLY');

-- VIEWER permissions (read-only, no explicit write permissions)
-- Viewers have implicit read access through the application layer

-- ============================================
-- Migrate existing task status/priority data to new entities
-- ============================================

-- Map old string status to new entity IDs
UPDATE tasks SET status_id = 'ts-todo' WHERE status = 'TODO' AND status_id IS NULL;
UPDATE tasks SET status_id = 'ts-in-progress' WHERE status = 'IN_PROGRESS' AND status_id IS NULL;
UPDATE tasks SET status_id = 'ts-done' WHERE status = 'DONE' AND status_id IS NULL;
UPDATE tasks SET status_id = 'ts-blocked' WHERE status = 'BLOCKED' AND status_id IS NULL;
UPDATE tasks SET status_id = 'ts-review' WHERE status = 'REVIEW' AND status_id IS NULL;
UPDATE tasks SET status_id = 'ts-cancelled' WHERE status = 'CANCELLED' AND status_id IS NULL;
-- Default fallback for any unmatched status
UPDATE tasks SET status_id = 'ts-todo' WHERE status_id IS NULL;

-- Map old string priority to new entity IDs
UPDATE tasks SET priority_id = 'tp-low' WHERE priority = 'LOW' AND priority_id IS NULL;
UPDATE tasks SET priority_id = 'tp-medium' WHERE (priority = 'MED' OR priority = 'MEDIUM') AND priority_id IS NULL;
UPDATE tasks SET priority_id = 'tp-high' WHERE priority = 'HIGH' AND priority_id IS NULL;
UPDATE tasks SET priority_id = 'tp-critical' WHERE priority = 'CRITICAL' AND priority_id IS NULL;
-- Default fallback
UPDATE tasks SET priority_id = 'tp-medium' WHERE priority_id IS NULL;

-- Migrate estimated/actual effort from old column names
UPDATE tasks SET estimated_effort = estimated_minutes WHERE estimated_effort IS NULL AND estimated_minutes IS NOT NULL;
UPDATE tasks SET actual_effort = actual_minutes WHERE actual_effort IS NULL AND actual_minutes IS NOT NULL;

-- ============================================
-- End V21
-- ============================================
