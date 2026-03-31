-- V57: Project Finance domain (Phase 18)

CREATE TABLE project_budgets (
  id VARCHAR(36) PRIMARY KEY,
  project_id VARCHAR(36) NOT NULL,
  name VARCHAR(255) NOT NULL,
  version INTEGER DEFAULT 1,
  status VARCHAR(32) DEFAULT 'DRAFT',
  currency VARCHAR(3) DEFAULT 'EUR',
  total_amount REAL,
  approved_by VARCHAR(36),
  approved_at TIMESTAMP,
  previous_version_id VARCHAR(36),
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  FOREIGN KEY (project_id) REFERENCES projects(id),
  FOREIGN KEY (approved_by) REFERENCES users(id)
);

CREATE TABLE budget_lines (
  id VARCHAR(36) PRIMARY KEY,
  budget_id VARCHAR(36) NOT NULL,
  name VARCHAR(255) NOT NULL,
  description TEXT,
  category VARCHAR(64) NOT NULL,
  phase_id VARCHAR(36),
  deliverable_id VARCHAR(36),
  planned_amount REAL DEFAULT 0,
  committed_amount REAL DEFAULT 0,
  reserved_amount REAL DEFAULT 0,
  actual_amount REAL DEFAULT 0,
  forecast_amount REAL DEFAULT 0,
  currency VARCHAR(3) DEFAULT 'EUR',
  sort_order INTEGER DEFAULT 0,
  FOREIGN KEY (budget_id) REFERENCES project_budgets(id),
  FOREIGN KEY (phase_id) REFERENCES phases(id),
  FOREIGN KEY (deliverable_id) REFERENCES deliverables(id)
);

CREATE TABLE funding_sources (
  id VARCHAR(36) PRIMARY KEY,
  project_id VARCHAR(36) NOT NULL,
  name VARCHAR(255) NOT NULL,
  type VARCHAR(32) NOT NULL,
  total_amount REAL,
  currency VARCHAR(3) DEFAULT 'EUR',
  is_restricted BOOLEAN DEFAULT FALSE,
  restriction_description TEXT,
  contact_name VARCHAR(255),
  contact_email VARCHAR(255),
  status VARCHAR(32) DEFAULT 'ACTIVE',
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  FOREIGN KEY (project_id) REFERENCES projects(id)
);

CREATE TABLE fund_allocations (
  id VARCHAR(36) PRIMARY KEY,
  funding_source_id VARCHAR(36) NOT NULL,
  budget_line_id VARCHAR(36) NOT NULL,
  allocated_amount REAL NOT NULL,
  allocation_date DATE,
  notes TEXT,
  FOREIGN KEY (funding_source_id) REFERENCES funding_sources(id),
  FOREIGN KEY (budget_line_id) REFERENCES budget_lines(id)
);

CREATE TABLE actual_cost_records (
  id VARCHAR(36) PRIMARY KEY,
  budget_line_id VARCHAR(36) NOT NULL,
  description VARCHAR(255) NOT NULL,
  amount REAL NOT NULL,
  currency VARCHAR(3) DEFAULT 'EUR',
  cost_date DATE NOT NULL,
  evidence_asset_id VARCHAR(36),
  recorded_by VARCHAR(36),
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  FOREIGN KEY (budget_line_id) REFERENCES budget_lines(id),
  FOREIGN KEY (evidence_asset_id) REFERENCES assets(id),
  FOREIGN KEY (recorded_by) REFERENCES users(id)
);

CREATE TABLE financial_baselines (
  id VARCHAR(36) PRIMARY KEY,
  budget_id VARCHAR(36) NOT NULL,
  name VARCHAR(255) NOT NULL,
  snapshot_json TEXT NOT NULL,
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  created_by VARCHAR(36),
  FOREIGN KEY (budget_id) REFERENCES project_budgets(id),
  FOREIGN KEY (created_by) REFERENCES users(id)
);

CREATE TABLE currency_rates (
  id VARCHAR(36) PRIMARY KEY,
  from_currency VARCHAR(3) NOT NULL,
  to_currency VARCHAR(3) NOT NULL,
  rate REAL NOT NULL,
  effective_date DATE NOT NULL,
  source VARCHAR(64),
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_budget_lines_budget ON budget_lines(budget_id);
CREATE INDEX idx_funding_sources_project ON funding_sources(project_id);
CREATE INDEX idx_fund_allocations_source ON fund_allocations(funding_source_id);
CREATE INDEX idx_actual_costs_line ON actual_cost_records(budget_line_id);
