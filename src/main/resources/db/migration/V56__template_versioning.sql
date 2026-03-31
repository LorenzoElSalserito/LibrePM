-- V56: Template versioning & vertical templates (Phase 16)
ALTER TABLE project_templates ADD COLUMN previous_version_id VARCHAR(36);
ALTER TABLE project_templates ADD COLUMN capability_profile TEXT;
ALTER TABLE project_templates ADD COLUMN complexity_level VARCHAR(16) DEFAULT 'SIMPLE';

-- Seed vertical system templates
UPDATE project_templates SET
  description = 'Planning with Gantt chart, phases and dependencies',
  category = 'PLANNING',
  use_cases = 'Waterfall projects, construction timelines, event planning',
  prerequisites = 'planning module enabled',
  version = '1.0',
  requires_planning_engine = TRUE,
  template_scope = 'SYSTEM',
  complexity_level = 'MODERATE',
  capability_profile = '{"planning":true}',
  structure_json = '{"phases":[{"name":"Initiation","order":1},{"name":"Planning","order":2},{"name":"Execution","order":3},{"name":"Closure","order":4}],"statuses":[{"name":"Not Started","color":"#6c757d","isDefault":true},{"name":"In Progress","color":"#0d6efd"},{"name":"Completed","color":"#198754","isFinal":true}],"tasks":[{"title":"Define scope","phase":"Initiation"},{"title":"Create WBS","phase":"Planning"},{"title":"Assign resources","phase":"Planning"},{"title":"Execute deliverables","phase":"Execution"},{"title":"Final review","phase":"Closure"}],"deliverables":[{"title":"Project Plan","phase":"Planning"},{"title":"Final Report","phase":"Closure"}]}',
  updated_at = CURRENT_TIMESTAMP
WHERE name = 'Project Timeline';

UPDATE project_templates SET
  description = 'Task management with status workflow and dashboard',
  category = 'PROJECT_MANAGEMENT',
  use_cases = 'Agile teams, sprint tracking, feature development',
  prerequisites = NULL,
  version = '1.0',
  requires_planning_engine = FALSE,
  template_scope = 'SYSTEM',
  complexity_level = 'SIMPLE',
  capability_profile = '{}',
  structure_json = '{"phases":[{"name":"Backlog","order":1},{"name":"Sprint","order":2},{"name":"Done","order":3}],"statuses":[{"name":"To Do","color":"#6c757d","isDefault":true},{"name":"In Progress","color":"#0d6efd"},{"name":"In Review","color":"#ffc107"},{"name":"Done","color":"#198754","isFinal":true}],"tasks":[{"title":"Define user stories","phase":"Backlog"},{"title":"Sprint planning","phase":"Sprint"},{"title":"Daily standup","phase":"Sprint"},{"title":"Sprint review","phase":"Done"}]}',
  updated_at = CURRENT_TIMESTAMP
WHERE name = 'Project Tracking';

UPDATE project_templates SET
  description = 'WBS, dependencies, baseline and critical path',
  category = 'PLANNING',
  use_cases = 'Large projects, engineering, infrastructure builds',
  prerequisites = 'planning module enabled',
  version = '1.0',
  requires_planning_engine = TRUE,
  template_scope = 'SYSTEM',
  complexity_level = 'ADVANCED',
  capability_profile = '{"planning":true}',
  structure_json = '{"phases":[{"name":"Design","order":1},{"name":"Build","order":2},{"name":"Test","order":3},{"name":"Deploy","order":4}],"statuses":[{"name":"Planned","color":"#6c757d","isDefault":true},{"name":"Active","color":"#0d6efd"},{"name":"Blocked","color":"#dc3545"},{"name":"Complete","color":"#198754","isFinal":true}],"tasks":[{"title":"Requirements analysis","phase":"Design"},{"title":"Architecture design","phase":"Design"},{"title":"Implementation","phase":"Build"},{"title":"Unit testing","phase":"Test"},{"title":"Integration testing","phase":"Test"},{"title":"Production deploy","phase":"Deploy"}],"deliverables":[{"title":"Design Document","phase":"Design"},{"title":"Release Package","phase":"Deploy"}],"dependencies":[{"from":"Requirements analysis","to":"Architecture design","type":"FS"},{"from":"Architecture design","to":"Implementation","type":"FS"},{"from":"Implementation","to":"Unit testing","type":"FS"}]}',
  updated_at = CURRENT_TIMESTAMP
WHERE name = 'Gantt Project';

INSERT INTO project_templates (id, name, description, category, use_cases, prerequisites, version, requires_planning_engine, template_scope, complexity_level, capability_profile, structure_json, created_at, updated_at) VALUES
  ('tpl-nonprofit', 'Nonprofit Grant Project', 'Budget, restricted funds, deliverables and reporting periods', 'NONPROFIT', 'NGO projects, development aid, social programs', 'planning and finance modules enabled', '1.0', TRUE, 'SYSTEM', 'ADVANCED', '{"planning":true,"finance":true,"grants":true}',
   '{"phases":[{"name":"Proposal","order":1},{"name":"Award","order":2},{"name":"Implementation","order":3},{"name":"Reporting","order":4},{"name":"Closeout","order":5}],"statuses":[{"name":"Draft","color":"#6c757d","isDefault":true},{"name":"Submitted","color":"#0dcaf0"},{"name":"Active","color":"#0d6efd"},{"name":"Complete","color":"#198754","isFinal":true}],"tasks":[{"title":"Write proposal","phase":"Proposal"},{"title":"Budget preparation","phase":"Proposal"},{"title":"Award acceptance","phase":"Award"},{"title":"Activity execution","phase":"Implementation"},{"title":"Mid-term report","phase":"Reporting"},{"title":"Final report","phase":"Reporting"},{"title":"Financial closeout","phase":"Closeout"}],"deliverables":[{"title":"Grant Proposal","phase":"Proposal"},{"title":"Mid-term Report","phase":"Reporting"},{"title":"Final Report","phase":"Reporting"},{"title":"Financial Statement","phase":"Closeout"}],"metrics":[{"name":"Budget Utilization","target":"95%"},{"name":"Beneficiaries Reached","target":"TBD"}]}',
   CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),

  ('tpl-publiccall', 'Public Call Application', 'Call registry, eligibility checklist and submission package', 'GRANTS', 'EU calls, public tenders, government grants', 'grants module enabled', '1.0', FALSE, 'SYSTEM', 'MODERATE', '{"planning":true,"grants":true}',
   '{"phases":[{"name":"Discovery","order":1},{"name":"Eligibility","order":2},{"name":"Preparation","order":3},{"name":"Submission","order":4},{"name":"Evaluation","order":5}],"statuses":[{"name":"Identified","color":"#6c757d","isDefault":true},{"name":"Eligible","color":"#0dcaf0"},{"name":"In Preparation","color":"#0d6efd"},{"name":"Submitted","color":"#ffc107"},{"name":"Awarded","color":"#198754","isFinal":true},{"name":"Rejected","color":"#dc3545","isFinal":true}],"tasks":[{"title":"Identify call","phase":"Discovery"},{"title":"Check eligibility criteria","phase":"Eligibility"},{"title":"Prepare technical annex","phase":"Preparation"},{"title":"Prepare budget","phase":"Preparation"},{"title":"Internal review","phase":"Preparation"},{"title":"Submit application","phase":"Submission"},{"title":"Await evaluation","phase":"Evaluation"}],"deliverables":[{"title":"Eligibility Checklist","phase":"Eligibility"},{"title":"Application Package","phase":"Submission"}]}',
   CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),

  ('tpl-sponsored', 'Sponsored Initiative', 'Sponsor commitments, stakeholder map and evidence tracking', 'STAKEHOLDER', 'Corporate sponsorships, CSR initiatives, partnership projects', 'finance module enabled', '1.0', FALSE, 'SYSTEM', 'MODERATE', '{"planning":true,"finance":true}',
   '{"phases":[{"name":"Engagement","order":1},{"name":"Agreement","order":2},{"name":"Delivery","order":3},{"name":"Reporting","order":4}],"statuses":[{"name":"Prospecting","color":"#6c757d","isDefault":true},{"name":"Committed","color":"#0d6efd"},{"name":"Active","color":"#198754"},{"name":"Closed","color":"#6c757d","isFinal":true}],"tasks":[{"title":"Identify sponsors","phase":"Engagement"},{"title":"Draft sponsorship agreement","phase":"Agreement"},{"title":"Execute deliverables","phase":"Delivery"},{"title":"Collect evidence","phase":"Delivery"},{"title":"Impact report","phase":"Reporting"}],"deliverables":[{"title":"Sponsorship Agreement","phase":"Agreement"},{"title":"Impact Report","phase":"Reporting"}]}',
   CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),

  ('tpl-consortium', 'R&D Consortium', 'Partners, work packages, co-funding and deliverable register', 'R_AND_D', 'Horizon Europe, national R&D programs, multi-partner research', 'all advanced modules', '1.0', TRUE, 'SYSTEM', 'ADVANCED', '{"planning":true,"finance":true,"grants":true,"portfolio":true}',
   '{"phases":[{"name":"WP1 - Management","order":1},{"name":"WP2 - Research","order":2},{"name":"WP3 - Development","order":3},{"name":"WP4 - Dissemination","order":4}],"statuses":[{"name":"Planned","color":"#6c757d","isDefault":true},{"name":"Active","color":"#0d6efd"},{"name":"Delivered","color":"#198754","isFinal":true}],"tasks":[{"title":"Consortium agreement","phase":"WP1 - Management"},{"title":"Progress monitoring","phase":"WP1 - Management"},{"title":"Literature review","phase":"WP2 - Research"},{"title":"Experimental design","phase":"WP2 - Research"},{"title":"Prototype development","phase":"WP3 - Development"},{"title":"Publication plan","phase":"WP4 - Dissemination"}],"deliverables":[{"title":"D1.1 Consortium Agreement","phase":"WP1 - Management"},{"title":"D2.1 State of the Art Report","phase":"WP2 - Research"},{"title":"D3.1 Prototype","phase":"WP3 - Development"},{"title":"D4.1 Dissemination Report","phase":"WP4 - Dissemination"}],"metrics":[{"name":"Publications","target":"3"},{"name":"Patents","target":"1"},{"name":"TRL Level","target":"6"}]}',
   CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),

  ('tpl-agency', 'Agency Delivery', 'Deliverables per client, budget per deliverable, timesheet', 'CONSULTING', 'Consulting firms, agencies, freelance project delivery', 'finance module enabled', '1.0', FALSE, 'SYSTEM', 'MODERATE', '{"planning":true,"finance":true}',
   '{"phases":[{"name":"Discovery","order":1},{"name":"Proposal","order":2},{"name":"Delivery","order":3},{"name":"Handoff","order":4}],"statuses":[{"name":"Scoping","color":"#6c757d","isDefault":true},{"name":"In Progress","color":"#0d6efd"},{"name":"Review","color":"#ffc107"},{"name":"Delivered","color":"#198754","isFinal":true}],"tasks":[{"title":"Client brief","phase":"Discovery"},{"title":"Scope definition","phase":"Discovery"},{"title":"Cost estimate","phase":"Proposal"},{"title":"Execute work","phase":"Delivery"},{"title":"Client review","phase":"Delivery"},{"title":"Final handoff","phase":"Handoff"}],"deliverables":[{"title":"Proposal Document","phase":"Proposal"},{"title":"Final Deliverable","phase":"Handoff"},{"title":"Invoice","phase":"Handoff"}]}',
   CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),

  ('tpl-operations', 'Internal Operations Programme', 'Portfolio overview, milestones and resource planning', 'OPERATIONS', 'IT operations, facilities management, organizational change', 'planning module enabled', '1.0', TRUE, 'SYSTEM', 'MODERATE', '{"planning":true,"portfolio":true}',
   '{"phases":[{"name":"Assessment","order":1},{"name":"Planning","order":2},{"name":"Rollout","order":3},{"name":"Stabilize","order":4}],"statuses":[{"name":"Pending","color":"#6c757d","isDefault":true},{"name":"Active","color":"#0d6efd"},{"name":"Stable","color":"#198754","isFinal":true}],"tasks":[{"title":"Current state assessment","phase":"Assessment"},{"title":"Define objectives","phase":"Planning"},{"title":"Resource allocation","phase":"Planning"},{"title":"Phased rollout","phase":"Rollout"},{"title":"Monitor & stabilize","phase":"Stabilize"}],"deliverables":[{"title":"Assessment Report","phase":"Assessment"},{"title":"Operations Plan","phase":"Planning"},{"title":"Rollout Checklist","phase":"Rollout"}]}',
   CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),

  ('tpl-portfolio', 'Portfolio Review Workspace', 'Cross-project overview, aggregated risk and funding concentration', 'PORTFOLIO', 'PMO dashboards, executive oversight, multi-project tracking', 'portfolio module enabled', '1.0', FALSE, 'SYSTEM', 'ADVANCED', '{"planning":true,"finance":true,"portfolio":true}',
   '{"phases":[{"name":"Setup","order":1},{"name":"Monitoring","order":2},{"name":"Review","order":3}],"statuses":[{"name":"Active","color":"#0d6efd","isDefault":true},{"name":"On Hold","color":"#ffc107"},{"name":"Closed","color":"#6c757d","isFinal":true}],"tasks":[{"title":"Define portfolio scope","phase":"Setup"},{"title":"Link sub-projects","phase":"Setup"},{"title":"Weekly status review","phase":"Monitoring"},{"title":"Risk aggregation","phase":"Monitoring"},{"title":"Quarterly portfolio review","phase":"Review"}],"metrics":[{"name":"Projects On Track","target":"80%"},{"name":"Budget Variance","target":"<10%"},{"name":"Resource Utilization","target":"85%"}]}',
   CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);
