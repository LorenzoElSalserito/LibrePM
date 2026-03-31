-- =============================================================
-- V59 — Change Control & Branches
-- =============================================================

CREATE TABLE change_requests (
  id VARCHAR(36) PRIMARY KEY,
  project_id VARCHAR(36) NOT NULL,
  title VARCHAR(255) NOT NULL,
  description TEXT,
  motivation TEXT,
  scope VARCHAR(32),
  priority VARCHAR(16) DEFAULT 'MEDIUM',
  expected_impact TEXT,
  status VARCHAR(32) DEFAULT 'DRAFT',
  requested_by VARCHAR(36),
  approved_by VARCHAR(36),
  requested_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  resolved_at TIMESTAMP,
  FOREIGN KEY (project_id) REFERENCES projects(id),
  FOREIGN KEY (requested_by) REFERENCES users(id),
  FOREIGN KEY (approved_by) REFERENCES users(id)
);

CREATE TABLE project_branches (
  id VARCHAR(36) PRIMARY KEY,
  source_project_id VARCHAR(36) NOT NULL,
  name VARCHAR(255) NOT NULL,
  description TEXT,
  branch_type VARCHAR(32) NOT NULL,
  status VARCHAR(32) DEFAULT 'ACTIVE',
  snapshot_json TEXT,
  change_request_id VARCHAR(36),
  created_by VARCHAR(36),
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  merged_at TIMESTAMP,
  FOREIGN KEY (source_project_id) REFERENCES projects(id),
  FOREIGN KEY (change_request_id) REFERENCES change_requests(id),
  FOREIGN KEY (created_by) REFERENCES users(id)
);

CREATE TABLE branch_modifications (
  id VARCHAR(36) PRIMARY KEY,
  branch_id VARCHAR(36) NOT NULL,
  entity_type VARCHAR(64) NOT NULL,
  entity_id VARCHAR(36) NOT NULL,
  modification_type VARCHAR(16) NOT NULL,
  old_value_json TEXT,
  new_value_json TEXT,
  modified_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  FOREIGN KEY (branch_id) REFERENCES project_branches(id)
);

CREATE TABLE decision_log (
  id VARCHAR(36) PRIMARY KEY,
  project_id VARCHAR(36) NOT NULL,
  branch_id VARCHAR(36),
  change_request_id VARCHAR(36),
  title VARCHAR(255) NOT NULL,
  decision TEXT NOT NULL,
  rationale TEXT,
  decided_by VARCHAR(36),
  decided_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  impact_summary TEXT,
  FOREIGN KEY (project_id) REFERENCES projects(id),
  FOREIGN KEY (branch_id) REFERENCES project_branches(id),
  FOREIGN KEY (change_request_id) REFERENCES change_requests(id),
  FOREIGN KEY (decided_by) REFERENCES users(id)
);

CREATE INDEX idx_change_requests_project ON change_requests(project_id, status);
CREATE INDEX idx_project_branches_source ON project_branches(source_project_id);
CREATE INDEX idx_branch_modifications ON branch_modifications(branch_id);
CREATE INDEX idx_decision_log_project ON decision_log(project_id);
