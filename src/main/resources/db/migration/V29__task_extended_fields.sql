-- V29: Extended task fields for planning, finance, and deliverable support (PRD-01-FR-004, PRD-01-FR-006)

-- Phase linkage (WBS node)
ALTER TABLE tasks ADD COLUMN phase_id TEXT REFERENCES wbs_nodes(id) ON DELETE SET NULL;

-- Deliverable and stakeholder linkage (FK added when those tables are created)
ALTER TABLE tasks ADD COLUMN deliverable_id TEXT;
ALTER TABLE tasks ADD COLUMN stakeholder_id TEXT;

-- Funding context (free text for now, structured in PRD-18)
ALTER TABLE tasks ADD COLUMN funding_context TEXT;

-- Progressive disclosure flags
ALTER TABLE tasks ADD COLUMN planning_enabled BOOLEAN DEFAULT FALSE;
ALTER TABLE tasks ADD COLUMN finance_enabled BOOLEAN DEFAULT FALSE;

-- Actual dates (distinct from planned dates)
ALTER TABLE tasks ADD COLUMN actual_start TEXT;
ALTER TABLE tasks ADD COLUMN actual_end TEXT;

-- Effort tracking in hours (separate from legacy minutes-based fields)
ALTER TABLE tasks ADD COLUMN estimated_effort_hours REAL;
ALTER TABLE tasks ADD COLUMN actual_effort_hours REAL;

-- Indexes for common queries
CREATE INDEX IF NOT EXISTS idx_tasks_phase ON tasks(phase_id);
CREATE INDEX IF NOT EXISTS idx_tasks_deliverable ON tasks(deliverable_id);
CREATE INDEX IF NOT EXISTS idx_tasks_planning ON tasks(planning_enabled);
CREATE INDEX IF NOT EXISTS idx_tasks_finance ON tasks(finance_enabled);
