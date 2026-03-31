-- V48: Calendar scopes (workspace, team, person, project)
ALTER TABLE work_calendars ADD COLUMN scope VARCHAR(32) DEFAULT 'WORKSPACE';
ALTER TABLE work_calendars ADD COLUMN scope_entity_id VARCHAR(36);
CREATE INDEX IF NOT EXISTS idx_work_calendars_scope ON work_calendars(scope, scope_entity_id);
