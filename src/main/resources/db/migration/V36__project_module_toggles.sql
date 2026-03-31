-- V36: Per-project module toggles (PRD-03-FR-005)
ALTER TABLE projects ADD COLUMN time_tracking_enabled BOOLEAN DEFAULT TRUE;
ALTER TABLE projects ADD COLUMN planning_enabled BOOLEAN DEFAULT FALSE;
ALTER TABLE projects ADD COLUMN finance_enabled BOOLEAN DEFAULT FALSE;
ALTER TABLE projects ADD COLUMN grants_enabled BOOLEAN DEFAULT FALSE;
