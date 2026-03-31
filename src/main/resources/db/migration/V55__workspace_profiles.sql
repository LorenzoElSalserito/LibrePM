-- V55: Workspace profiles (PRD-15-FR-002)
CREATE TABLE IF NOT EXISTS workspace_profiles (
  id VARCHAR(36) PRIMARY KEY,
  name VARCHAR(64) NOT NULL,
  description TEXT,
  modules_json TEXT NOT NULL,
  nav_items_json TEXT,
  is_system BOOLEAN DEFAULT FALSE,
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- System preset profiles
INSERT INTO workspace_profiles (id, name, description, modules_json, is_system) VALUES
  ('wp-personal', 'Personal', 'Tasks, notes, lightweight planning', '{"planning":false,"finance":false,"grants":false,"portfolio":false,"branches":false}', TRUE),
  ('wp-team', 'Team Operations', 'Collaboration, boards, timeline, workload', '{"planning":true,"finance":false,"grants":false,"portfolio":false,"branches":false}', TRUE),
  ('wp-pm', 'PM / PMO Light', 'Baselines, forecasts, resource planning', '{"planning":true,"finance":true,"grants":false,"portfolio":false,"branches":true}', TRUE),
  ('wp-nonprofit', 'Nonprofit & Donations', 'Funds, donors, reporting', '{"planning":true,"finance":true,"grants":true,"portfolio":false,"branches":false}', TRUE),
  ('wp-grant', 'Grant & Calls', 'Grants, eligibility, submissions', '{"planning":true,"finance":true,"grants":true,"portfolio":false,"branches":true}', TRUE),
  ('wp-agency', 'Agency / Consultancy', 'Budget per deliverable, stakeholders', '{"planning":true,"finance":true,"grants":false,"portfolio":false,"branches":true}', TRUE),
  ('wp-rd', 'R&D Consortium', 'Partners, work packages, co-funding', '{"planning":true,"finance":true,"grants":true,"portfolio":true,"branches":true}', TRUE);

ALTER TABLE users ADD COLUMN workspace_profile_id VARCHAR(36) DEFAULT 'wp-personal';
