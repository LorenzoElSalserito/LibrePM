-- =============================================================
-- V58 — Grants, Calls, Sponsors & Stakeholder tables
-- =============================================================

CREATE TABLE grant_calls (
  id VARCHAR(36) PRIMARY KEY,
  project_id VARCHAR(36),
  title VARCHAR(255) NOT NULL,
  description TEXT,
  issuer VARCHAR(255),
  reference_code VARCHAR(128),
  status VARCHAR(32) DEFAULT 'IDENTIFIED',
  deadline DATE,
  budget_available REAL,
  currency VARCHAR(3) DEFAULT 'EUR',
  url TEXT,
  notes TEXT,
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  FOREIGN KEY (project_id) REFERENCES projects(id)
);

CREATE TABLE call_requirements (
  id VARCHAR(36) PRIMARY KEY,
  call_id VARCHAR(36) NOT NULL,
  description TEXT NOT NULL,
  requirement_type VARCHAR(32),
  is_met BOOLEAN DEFAULT FALSE,
  evidence_note TEXT,
  sort_order INTEGER DEFAULT 0,
  FOREIGN KEY (call_id) REFERENCES grant_calls(id)
);

CREATE TABLE submission_packages (
  id VARCHAR(36) PRIMARY KEY,
  call_id VARCHAR(36) NOT NULL,
  status VARCHAR(32) DEFAULT 'DRAFT',
  submitted_at TIMESTAMP,
  submitted_by VARCHAR(36),
  notes TEXT,
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  FOREIGN KEY (call_id) REFERENCES grant_calls(id),
  FOREIGN KEY (submitted_by) REFERENCES users(id)
);

CREATE TABLE donors (
  id VARCHAR(36) PRIMARY KEY,
  name VARCHAR(255) NOT NULL,
  organization VARCHAR(255),
  email VARCHAR(255),
  phone VARCHAR(64),
  type VARCHAR(32),
  notes TEXT,
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE donations (
  id VARCHAR(36) PRIMARY KEY,
  donor_id VARCHAR(36) NOT NULL,
  project_id VARCHAR(36),
  amount REAL NOT NULL,
  currency VARCHAR(3) DEFAULT 'EUR',
  donation_date DATE,
  is_restricted BOOLEAN DEFAULT FALSE,
  restriction_description TEXT,
  receipt_asset_id VARCHAR(36),
  notes TEXT,
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  FOREIGN KEY (donor_id) REFERENCES donors(id),
  FOREIGN KEY (project_id) REFERENCES projects(id),
  FOREIGN KEY (receipt_asset_id) REFERENCES assets(id)
);

CREATE TABLE sponsors (
  id VARCHAR(36) PRIMARY KEY,
  name VARCHAR(255) NOT NULL,
  organization VARCHAR(255),
  email VARCHAR(255),
  contact_person VARCHAR(255),
  type VARCHAR(32),
  notes TEXT,
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE sponsor_commitments (
  id VARCHAR(36) PRIMARY KEY,
  sponsor_id VARCHAR(36) NOT NULL,
  project_id VARCHAR(36) NOT NULL,
  description TEXT,
  committed_amount REAL,
  currency VARCHAR(3) DEFAULT 'EUR',
  status VARCHAR(32) DEFAULT 'PROPOSED',
  agreement_date DATE,
  agreement_asset_id VARCHAR(36),
  FOREIGN KEY (sponsor_id) REFERENCES sponsors(id),
  FOREIGN KEY (project_id) REFERENCES projects(id),
  FOREIGN KEY (agreement_asset_id) REFERENCES assets(id)
);

CREATE TABLE stakeholders (
  id VARCHAR(36) PRIMARY KEY,
  project_id VARCHAR(36) NOT NULL,
  name VARCHAR(255) NOT NULL,
  organization VARCHAR(255),
  role_description VARCHAR(255),
  influence_level VARCHAR(16),
  interest_level VARCHAR(16),
  engagement_strategy TEXT,
  channel VARCHAR(64),
  notes TEXT,
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  FOREIGN KEY (project_id) REFERENCES projects(id)
);

CREATE TABLE partner_organisations (
  id VARCHAR(36) PRIMARY KEY,
  project_id VARCHAR(36) NOT NULL,
  name VARCHAR(255) NOT NULL,
  country VARCHAR(64),
  role_in_project VARCHAR(128),
  contact_person VARCHAR(255),
  contact_email VARCHAR(255),
  budget_share REAL,
  notes TEXT,
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  FOREIGN KEY (project_id) REFERENCES projects(id)
);

CREATE TABLE obligation_records (
  id VARCHAR(36) PRIMARY KEY,
  project_id VARCHAR(36) NOT NULL,
  title VARCHAR(255) NOT NULL,
  description TEXT,
  type VARCHAR(32),
  deadline DATE,
  responsible_user_id VARCHAR(36),
  status VARCHAR(32) DEFAULT 'PENDING',
  linked_deliverable_id VARCHAR(36),
  linked_budget_line_id VARCHAR(36),
  notes TEXT,
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  FOREIGN KEY (project_id) REFERENCES projects(id),
  FOREIGN KEY (responsible_user_id) REFERENCES users(id),
  FOREIGN KEY (linked_deliverable_id) REFERENCES deliverables(id),
  FOREIGN KEY (linked_budget_line_id) REFERENCES budget_lines(id)
);

CREATE TABLE reporting_periods (
  id VARCHAR(36) PRIMARY KEY,
  project_id VARCHAR(36) NOT NULL,
  name VARCHAR(128) NOT NULL,
  period_start DATE NOT NULL,
  period_end DATE NOT NULL,
  due_date DATE,
  status VARCHAR(32) DEFAULT 'UPCOMING',
  submission_asset_id VARCHAR(36),
  notes TEXT,
  FOREIGN KEY (project_id) REFERENCES projects(id),
  FOREIGN KEY (submission_asset_id) REFERENCES assets(id)
);

-- Indexes
CREATE INDEX idx_grant_calls_project ON grant_calls(project_id);
CREATE INDEX idx_call_requirements_call ON call_requirements(call_id);
CREATE INDEX idx_donations_project ON donations(project_id);
CREATE INDEX idx_stakeholders_project ON stakeholders(project_id);
CREATE INDEX idx_obligations_project ON obligation_records(project_id, status);
CREATE INDEX idx_reporting_periods ON reporting_periods(project_id, due_date);
