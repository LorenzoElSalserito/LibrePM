-- V37: System roles and granular permissions (PRD-04-FR-001)

-- Seed system roles
INSERT OR IGNORE INTO roles (id, name, description, created_at, updated_at, sync_status) VALUES
  ('role-member',         'Member',          'Standard operational member',                     CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'LOCAL_ONLY'),
  ('role-lead',           'Lead',            'Team/Project lead with extended visibility',      CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'LOCAL_ONLY'),
  ('role-planner',        'Planner',         'Access to planning, dependencies, Gantt',         CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'LOCAL_ONLY'),
  ('role-finance-mgr',    'Finance Manager', 'Access to budgets, funds, costs',                 CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'LOCAL_ONLY'),
  ('role-grant-mgr',      'Grant Manager',   'Access to grants, calls, submissions',            CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'LOCAL_ONLY'),
  ('role-sponsor-viewer', 'Sponsor Viewer',  'Read-only access to dashboards and charter',      CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'LOCAL_ONLY'),
  ('role-auditor',        'Auditor',         'Access to evidence and audit packages (read-only)', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'LOCAL_ONLY'),
  ('role-admin',          'Admin',           'Full workspace administration',                   CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'LOCAL_ONLY');

-- Seed permission policies
-- Member permissions
INSERT OR IGNORE INTO permission_policies (id, role_id, permission, created_at, updated_at, sync_status) VALUES
  ('pp-m-01', 'role-member', 'TASK_READ',           CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'LOCAL_ONLY'),
  ('pp-m-02', 'role-member', 'TASK_WRITE',          CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'LOCAL_ONLY'),
  ('pp-m-03', 'role-member', 'NOTE_READ',           CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'LOCAL_ONLY'),
  ('pp-m-04', 'role-member', 'NOTE_WRITE',          CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'LOCAL_ONLY'),
  ('pp-m-05', 'role-member', 'TIME_ENTRY_READ',     CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'LOCAL_ONLY'),
  ('pp-m-06', 'role-member', 'TIME_ENTRY_WRITE',    CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'LOCAL_ONLY');

-- Lead permissions (all member + project management)
INSERT OR IGNORE INTO permission_policies (id, role_id, permission, created_at, updated_at, sync_status) VALUES
  ('pp-l-01', 'role-lead', 'TASK_READ',             CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'LOCAL_ONLY'),
  ('pp-l-02', 'role-lead', 'TASK_WRITE',            CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'LOCAL_ONLY'),
  ('pp-l-03', 'role-lead', 'NOTE_READ',             CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'LOCAL_ONLY'),
  ('pp-l-04', 'role-lead', 'NOTE_WRITE',            CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'LOCAL_ONLY'),
  ('pp-l-05', 'role-lead', 'TIME_ENTRY_READ',       CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'LOCAL_ONLY'),
  ('pp-l-06', 'role-lead', 'TIME_ENTRY_WRITE',      CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'LOCAL_ONLY'),
  ('pp-l-07', 'role-lead', 'PROJECT_SETTINGS',      CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'LOCAL_ONLY'),
  ('pp-l-08', 'role-lead', 'MEMBER_MANAGE',         CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'LOCAL_ONLY'),
  ('pp-l-09', 'role-lead', 'DELIVERABLE_READ',      CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'LOCAL_ONLY'),
  ('pp-l-10', 'role-lead', 'DELIVERABLE_WRITE',     CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'LOCAL_ONLY');

-- Planner permissions
INSERT OR IGNORE INTO permission_policies (id, role_id, permission, created_at, updated_at, sync_status) VALUES
  ('pp-p-01', 'role-planner', 'TASK_READ',          CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'LOCAL_ONLY'),
  ('pp-p-02', 'role-planner', 'TASK_WRITE',         CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'LOCAL_ONLY'),
  ('pp-p-03', 'role-planner', 'GANTT_READ',         CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'LOCAL_ONLY'),
  ('pp-p-04', 'role-planner', 'GANTT_WRITE',        CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'LOCAL_ONLY'),
  ('pp-p-05', 'role-planner', 'DEPENDENCY_READ',    CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'LOCAL_ONLY'),
  ('pp-p-06', 'role-planner', 'DEPENDENCY_WRITE',   CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'LOCAL_ONLY'),
  ('pp-p-07', 'role-planner', 'BASELINE_READ',      CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'LOCAL_ONLY'),
  ('pp-p-08', 'role-planner', 'BASELINE_WRITE',     CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'LOCAL_ONLY');

-- Finance Manager permissions
INSERT OR IGNORE INTO permission_policies (id, role_id, permission, created_at, updated_at, sync_status) VALUES
  ('pp-f-01', 'role-finance-mgr', 'BUDGET_READ',    CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'LOCAL_ONLY'),
  ('pp-f-02', 'role-finance-mgr', 'BUDGET_WRITE',   CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'LOCAL_ONLY'),
  ('pp-f-03', 'role-finance-mgr', 'COST_READ',      CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'LOCAL_ONLY'),
  ('pp-f-04', 'role-finance-mgr', 'COST_WRITE',     CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'LOCAL_ONLY'),
  ('pp-f-05', 'role-finance-mgr', 'TASK_READ',      CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'LOCAL_ONLY'),
  ('pp-f-06', 'role-finance-mgr', 'TIME_ENTRY_READ',CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'LOCAL_ONLY');

-- Grant Manager permissions
INSERT OR IGNORE INTO permission_policies (id, role_id, permission, created_at, updated_at, sync_status) VALUES
  ('pp-g-01', 'role-grant-mgr', 'GRANT_READ',       CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'LOCAL_ONLY'),
  ('pp-g-02', 'role-grant-mgr', 'GRANT_WRITE',      CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'LOCAL_ONLY'),
  ('pp-g-03', 'role-grant-mgr', 'STAKEHOLDER_READ', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'LOCAL_ONLY'),
  ('pp-g-04', 'role-grant-mgr', 'STAKEHOLDER_WRITE',CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'LOCAL_ONLY'),
  ('pp-g-05', 'role-grant-mgr', 'TASK_READ',        CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'LOCAL_ONLY'),
  ('pp-g-06', 'role-grant-mgr', 'NOTE_READ',        CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'LOCAL_ONLY');

-- Sponsor Viewer permissions (read-only)
INSERT OR IGNORE INTO permission_policies (id, role_id, permission, created_at, updated_at, sync_status) VALUES
  ('pp-sv-01', 'role-sponsor-viewer', 'DASHBOARD_READ', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'LOCAL_ONLY'),
  ('pp-sv-02', 'role-sponsor-viewer', 'CHARTER_READ',   CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'LOCAL_ONLY'),
  ('pp-sv-03', 'role-sponsor-viewer', 'DELIVERABLE_READ', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'LOCAL_ONLY');

-- Auditor permissions (read-only + export)
INSERT OR IGNORE INTO permission_policies (id, role_id, permission, created_at, updated_at, sync_status) VALUES
  ('pp-a-01', 'role-auditor', 'EVIDENCE_READ',          CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'LOCAL_ONLY'),
  ('pp-a-02', 'role-auditor', 'AUDIT_PACKAGE_READ',     CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'LOCAL_ONLY'),
  ('pp-a-03', 'role-auditor', 'AUDIT_PACKAGE_EXPORT',   CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'LOCAL_ONLY'),
  ('pp-a-04', 'role-auditor', 'NOTE_READ',              CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'LOCAL_ONLY'),
  ('pp-a-05', 'role-auditor', 'TASK_READ',              CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'LOCAL_ONLY');

-- Admin permissions (everything)
INSERT OR IGNORE INTO permission_policies (id, role_id, permission, created_at, updated_at, sync_status) VALUES
  ('pp-ad-01', 'role-admin', 'TASK_READ',           CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'LOCAL_ONLY'),
  ('pp-ad-02', 'role-admin', 'TASK_WRITE',          CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'LOCAL_ONLY'),
  ('pp-ad-03', 'role-admin', 'NOTE_READ',           CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'LOCAL_ONLY'),
  ('pp-ad-04', 'role-admin', 'NOTE_WRITE',          CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'LOCAL_ONLY'),
  ('pp-ad-05', 'role-admin', 'TIME_ENTRY_READ',     CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'LOCAL_ONLY'),
  ('pp-ad-06', 'role-admin', 'TIME_ENTRY_WRITE',    CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'LOCAL_ONLY'),
  ('pp-ad-07', 'role-admin', 'PROJECT_SETTINGS',    CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'LOCAL_ONLY'),
  ('pp-ad-08', 'role-admin', 'MEMBER_MANAGE',       CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'LOCAL_ONLY'),
  ('pp-ad-09', 'role-admin', 'BUDGET_READ',         CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'LOCAL_ONLY'),
  ('pp-ad-10', 'role-admin', 'BUDGET_WRITE',        CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'LOCAL_ONLY'),
  ('pp-ad-11', 'role-admin', 'COST_READ',           CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'LOCAL_ONLY'),
  ('pp-ad-12', 'role-admin', 'COST_WRITE',          CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'LOCAL_ONLY'),
  ('pp-ad-13', 'role-admin', 'GRANT_READ',          CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'LOCAL_ONLY'),
  ('pp-ad-14', 'role-admin', 'GRANT_WRITE',         CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'LOCAL_ONLY'),
  ('pp-ad-15', 'role-admin', 'STAKEHOLDER_READ',    CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'LOCAL_ONLY'),
  ('pp-ad-16', 'role-admin', 'STAKEHOLDER_WRITE',   CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'LOCAL_ONLY'),
  ('pp-ad-17', 'role-admin', 'GANTT_READ',          CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'LOCAL_ONLY'),
  ('pp-ad-18', 'role-admin', 'GANTT_WRITE',         CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'LOCAL_ONLY'),
  ('pp-ad-19', 'role-admin', 'DEPENDENCY_READ',     CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'LOCAL_ONLY'),
  ('pp-ad-20', 'role-admin', 'DEPENDENCY_WRITE',    CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'LOCAL_ONLY'),
  ('pp-ad-21', 'role-admin', 'BASELINE_READ',       CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'LOCAL_ONLY'),
  ('pp-ad-22', 'role-admin', 'BASELINE_WRITE',      CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'LOCAL_ONLY'),
  ('pp-ad-23', 'role-admin', 'DELIVERABLE_READ',    CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'LOCAL_ONLY'),
  ('pp-ad-24', 'role-admin', 'DELIVERABLE_WRITE',   CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'LOCAL_ONLY'),
  ('pp-ad-25', 'role-admin', 'EVIDENCE_READ',       CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'LOCAL_ONLY'),
  ('pp-ad-26', 'role-admin', 'AUDIT_PACKAGE_READ',  CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'LOCAL_ONLY'),
  ('pp-ad-27', 'role-admin', 'AUDIT_PACKAGE_EXPORT',CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'LOCAL_ONLY'),
  ('pp-ad-28', 'role-admin', 'DASHBOARD_READ',      CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'LOCAL_ONLY'),
  ('pp-ad-29', 'role-admin', 'CHARTER_READ',        CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'LOCAL_ONLY');

-- Add system_role column to project_members for linking to roles table
ALTER TABLE project_members ADD COLUMN system_role_id TEXT REFERENCES roles(id);
