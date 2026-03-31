-- =============================================================
-- V61 — Portfolio Light (Programmes)
-- =============================================================

CREATE TABLE programmes (
  id VARCHAR(36) PRIMARY KEY,
  name VARCHAR(255) NOT NULL,
  description TEXT,
  owner_id VARCHAR(36),
  status VARCHAR(32) DEFAULT 'ACTIVE',
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  FOREIGN KEY (owner_id) REFERENCES users(id)
);

CREATE TABLE programme_memberships (
  id VARCHAR(36) PRIMARY KEY,
  programme_id VARCHAR(36) NOT NULL,
  project_id VARCHAR(36) NOT NULL,
  sort_order INTEGER DEFAULT 0,
  FOREIGN KEY (programme_id) REFERENCES programmes(id),
  FOREIGN KEY (project_id) REFERENCES projects(id),
  UNIQUE(programme_id, project_id)
);

CREATE TABLE programme_milestones (
  id VARCHAR(36) PRIMARY KEY,
  programme_id VARCHAR(36) NOT NULL,
  name VARCHAR(255) NOT NULL,
  description TEXT,
  target_date DATE,
  status VARCHAR(32) DEFAULT 'PENDING',
  linked_project_id VARCHAR(36),
  FOREIGN KEY (programme_id) REFERENCES programmes(id),
  FOREIGN KEY (linked_project_id) REFERENCES projects(id)
);

CREATE INDEX idx_programme_memberships ON programme_memberships(programme_id);
CREATE INDEX idx_programme_milestones ON programme_milestones(programme_id);
