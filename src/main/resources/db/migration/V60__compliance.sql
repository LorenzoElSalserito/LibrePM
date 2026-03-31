-- =============================================================
-- V60 — Privacy, Compliance & Data Classification
-- =============================================================

CREATE TABLE data_categories (
  id VARCHAR(36) PRIMARY KEY,
  name VARCHAR(128) NOT NULL,
  description TEXT,
  sensitivity_level VARCHAR(16) DEFAULT 'NORMAL',
  default_retention_days INTEGER,
  processing_purposes TEXT
);

CREATE TABLE retention_policies (
  id VARCHAR(36) PRIMARY KEY,
  data_category_id VARCHAR(36) NOT NULL,
  scope VARCHAR(32) DEFAULT 'WORKSPACE',
  scope_entity_id VARCHAR(36),
  retention_days INTEGER NOT NULL,
  action_on_expiry VARCHAR(32) DEFAULT 'ARCHIVE',
  is_active BOOLEAN DEFAULT TRUE,
  FOREIGN KEY (data_category_id) REFERENCES data_categories(id)
);

CREATE TABLE dsr_requests (
  id VARCHAR(36) PRIMARY KEY,
  user_id VARCHAR(36) NOT NULL,
  request_type VARCHAR(32) NOT NULL,
  status VARCHAR(32) DEFAULT 'PENDING',
  description TEXT,
  requested_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  completed_at TIMESTAMP,
  result_notes TEXT,
  FOREIGN KEY (user_id) REFERENCES users(id)
);

CREATE TABLE compliance_profiles (
  id VARCHAR(36) PRIMARY KEY,
  name VARCHAR(128) NOT NULL,
  description TEXT,
  settings_json TEXT NOT NULL,
  is_system BOOLEAN DEFAULT TRUE
);

-- Seed data categories
INSERT INTO data_categories (id, name, description, sensitivity_level, default_retention_days) VALUES
  ('dc-personal', 'Personal Data', 'User identification data', 'SENSITIVE', 1095),
  ('dc-financial', 'Financial Data', 'Budget, cost, funding data', 'RESTRICTED', 3650),
  ('dc-project', 'Project Data', 'Tasks, notes, deliverables', 'NORMAL', NULL),
  ('dc-evidence', 'Evidence', 'Evidence, attachments, audit', 'SENSITIVE', 3650),
  ('dc-audit', 'Audit Logs', 'Audit and journal logs', 'NORMAL', 730);

-- Seed compliance profiles
INSERT INTO compliance_profiles (id, name, description, settings_json, is_system) VALUES
  ('cp-standard', 'Standard', 'Default compliance profile', '{"dataRetention":true,"auditLog":true,"dsrEnabled":true}', TRUE),
  ('cp-nonprofit', 'Non-Profit', 'Enhanced compliance for NGOs', '{"dataRetention":true,"auditLog":true,"dsrEnabled":true,"donorPrivacy":true}', TRUE),
  ('cp-grant', 'Grant-Funded', 'Compliance for grant-funded projects', '{"dataRetention":true,"auditLog":true,"dsrEnabled":true,"evidenceRetention":true,"reportingCompliance":true}', TRUE);

CREATE INDEX idx_retention_policies_category ON retention_policies(data_category_id);
CREATE INDEX idx_dsr_requests_user ON dsr_requests(user_id, status);
