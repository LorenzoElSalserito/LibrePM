-- V35: Effort estimate revisions and project-level variance support (PRD-03-FR-002/003)
ALTER TABLE effort_estimates ADD COLUMN revision_number INTEGER DEFAULT 1;
ALTER TABLE effort_estimates ADD COLUMN previous_estimate_id VARCHAR(36);
CREATE INDEX idx_estimate_revision ON effort_estimates(task_id, revision_number);
