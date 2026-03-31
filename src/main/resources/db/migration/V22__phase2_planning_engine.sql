-- ============================================
-- V22: Phase 2 - Advanced Planning Engine
-- Includes: Dependencies, WBS, Work Calendars,
--           Capacity Engine, Task Hierarchy
-- ============================================

-- Add parent_task_id to tasks (SUMMARY_TASK hierarchy)
ALTER TABLE tasks ADD COLUMN parent_task_id TEXT REFERENCES tasks(id);
CREATE INDEX IF NOT EXISTS idx_task_parent ON tasks(parent_task_id);

-- Dependencies between tasks (rich model with type, lag, lead)
CREATE TABLE IF NOT EXISTS dependencies (
    id TEXT PRIMARY KEY,
    predecessor_id TEXT NOT NULL REFERENCES tasks(id) ON DELETE CASCADE,
    successor_id   TEXT NOT NULL REFERENCES tasks(id) ON DELETE CASCADE,
    type           TEXT NOT NULL DEFAULT 'FINISH_TO_START',
    lag            INTEGER,           -- minutes (positive = delay successor)
    lead           INTEGER,           -- minutes (positive = overlap/advance)
    created_at     TEXT NOT NULL,
    updated_at     TEXT NOT NULL,
    deleted_at     TEXT,
    last_synced_at TEXT,
    sync_status    TEXT DEFAULT 'LOCAL_ONLY',
    UNIQUE(predecessor_id, successor_id)
);
CREATE INDEX IF NOT EXISTS idx_dependency_predecessor ON dependencies(predecessor_id);
CREATE INDEX IF NOT EXISTS idx_dependency_successor   ON dependencies(successor_id);

-- WBS Nodes (Work Breakdown Structure hierarchy)
CREATE TABLE IF NOT EXISTS wbs_nodes (
    id          TEXT PRIMARY KEY,
    project_id  TEXT NOT NULL REFERENCES projects(id) ON DELETE CASCADE,
    parent_id   TEXT REFERENCES wbs_nodes(id) ON DELETE CASCADE,
    task_id     TEXT REFERENCES tasks(id) ON DELETE SET NULL,
    name        TEXT NOT NULL,
    wbs_code    TEXT NOT NULL,
    sort_order  INTEGER NOT NULL DEFAULT 0,
    created_at  TEXT NOT NULL,
    updated_at  TEXT NOT NULL,
    deleted_at  TEXT,
    last_synced_at TEXT,
    sync_status TEXT DEFAULT 'LOCAL_ONLY'
);
CREATE INDEX IF NOT EXISTS idx_wbs_project ON wbs_nodes(project_id);
CREATE INDEX IF NOT EXISTS idx_wbs_parent  ON wbs_nodes(parent_id);

-- Work Calendars (PRD-09)
CREATE TABLE IF NOT EXISTS work_calendars (
    id          TEXT PRIMARY KEY,
    name        TEXT NOT NULL UNIQUE,
    description TEXT,
    created_at  TEXT NOT NULL,
    updated_at  TEXT NOT NULL,
    deleted_at  TEXT,
    last_synced_at TEXT,
    sync_status TEXT DEFAULT 'LOCAL_ONLY'
);

-- Work Day Rules (working hours per day of week)
CREATE TABLE IF NOT EXISTS work_day_rules (
    id              TEXT PRIMARY KEY,
    calendar_id     TEXT NOT NULL REFERENCES work_calendars(id) ON DELETE CASCADE,
    day_of_week     TEXT NOT NULL,           -- MONDAY, TUESDAY, ...
    is_working_day  INTEGER NOT NULL DEFAULT 1,
    start_time      TEXT,                    -- e.g. '09:00'
    end_time        TEXT,                    -- e.g. '17:00'
    break_start_time TEXT,
    break_end_time   TEXT,
    created_at      TEXT NOT NULL,
    updated_at      TEXT NOT NULL,
    deleted_at      TEXT,
    last_synced_at  TEXT,
    sync_status     TEXT DEFAULT 'LOCAL_ONLY',
    UNIQUE(calendar_id, day_of_week)
);

-- Calendar Exceptions (holidays, special days)
CREATE TABLE IF NOT EXISTS calendar_exceptions (
    id             TEXT PRIMARY KEY,
    calendar_id    TEXT NOT NULL REFERENCES work_calendars(id) ON DELETE CASCADE,
    exception_date TEXT NOT NULL,
    is_working_day INTEGER NOT NULL DEFAULT 0,
    description    TEXT,
    created_at     TEXT NOT NULL,
    updated_at     TEXT NOT NULL,
    deleted_at     TEXT,
    last_synced_at TEXT,
    sync_status    TEXT DEFAULT 'LOCAL_ONLY',
    UNIQUE(calendar_id, exception_date)
);
CREATE INDEX IF NOT EXISTS idx_exception_calendar ON calendar_exceptions(calendar_id);
CREATE INDEX IF NOT EXISTS idx_exception_date     ON calendar_exceptions(exception_date);

-- ============================================
-- Seed: Default "Standard" Work Calendar
-- Mon-Fri 09:00-17:00 (PRD-09-BR-001)
-- ============================================

INSERT INTO work_calendars (id, name, description, created_at, updated_at, sync_status)
VALUES (
    lower(hex(randomblob(4))) || '-' || lower(hex(randomblob(2))) || '-4' ||
    substr(lower(hex(randomblob(2))),2) || '-' ||
    substr('89ab', abs(random()) % 4 + 1, 1) ||
    substr(lower(hex(randomblob(2))),2) || '-' ||
    lower(hex(randomblob(6))),
    'Standard',
    'Calendario lavorativo standard Mon-Ven 9-17',
    datetime('now'),
    datetime('now'),
    'LOCAL_ONLY'
);

-- Work Day Rules for Standard calendar (Mon-Fri working, Sat-Sun non-working)
INSERT INTO work_day_rules (id, calendar_id, day_of_week, is_working_day, start_time, end_time, created_at, updated_at, sync_status)
SELECT
    lower(hex(randomblob(4))) || '-' || lower(hex(randomblob(2))) || '-4' ||
    substr(lower(hex(randomblob(2))),2) || '-' ||
    substr('89ab', abs(random()) % 4 + 1, 1) ||
    substr(lower(hex(randomblob(2))),2) || '-' ||
    lower(hex(randomblob(6))),
    wc.id,
    day_info.day,
    day_info.working,
    day_info.start_t,
    day_info.end_t,
    datetime('now'),
    datetime('now'),
    'LOCAL_ONLY'
FROM work_calendars wc,
(
    SELECT 'MONDAY'    AS day, 1 AS working, '09:00' AS start_t, '17:00' AS end_t UNION ALL
    SELECT 'TUESDAY',   1, '09:00', '17:00' UNION ALL
    SELECT 'WEDNESDAY', 1, '09:00', '17:00' UNION ALL
    SELECT 'THURSDAY',  1, '09:00', '17:00' UNION ALL
    SELECT 'FRIDAY',    1, '09:00', '17:00' UNION ALL
    SELECT 'SATURDAY',  0, NULL,    NULL    UNION ALL
    SELECT 'SUNDAY',    0, NULL,    NULL
) day_info
WHERE wc.name = 'Standard';
