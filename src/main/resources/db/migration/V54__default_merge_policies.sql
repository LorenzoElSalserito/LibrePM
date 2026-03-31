-- V54: Seed default merge policies per entity type (PRD-13-FR-002)
INSERT OR IGNORE INTO merge_policies (id, entity_type, policy, description, auto_resolvable, field_scope, created_at, updated_at)
VALUES
  (lower(hex(randomblob(16))), 'Task', 'LAST_WRITE_WINS', 'Text fields use LWW with conflict on concurrent edits', 1, 'title,description,notes', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
  (lower(hex(randomblob(16))), 'Note', 'LAST_WRITE_WINS', 'Notes use LWW for content fields', 1, 'title,content', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
  (lower(hex(randomblob(16))), 'Tag', 'UNION', 'Tags merge by set union', 1, '*', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
  (lower(hex(randomblob(16))), 'TaskChecklistItem', 'LIST_MERGE', 'Checklist items merge at item level preserving order', 1, '*', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
  (lower(hex(randomblob(16))), 'Dependency', 'GRAPH_MERGE', 'Dependencies require manual review to avoid cycles', 0, '*', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
  (lower(hex(randomblob(16))), 'Baseline', 'IMMUTABLE_SNAPSHOT', 'Baselines are immutable — remote updates ignored', 1, '*', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
  (lower(hex(randomblob(16))), 'AuditEvent', 'APPEND_ONLY', 'Audit events append from both sides', 1, '*', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
  (lower(hex(randomblob(16))), 'RiskRegisterEntry', 'MANUAL', 'Risk entries require manual review', 0, '*', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
  (lower(hex(randomblob(16))), 'Deliverable', 'MANUAL', 'Deliverable changes require manual review', 0, '*', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
  (lower(hex(randomblob(16))), 'ProjectCharter', 'MANUAL', 'Charter changes require manual review', 0, '*', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);
