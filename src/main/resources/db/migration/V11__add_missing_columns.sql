-- V11: Add missing columns and tables for Task features and UserSettings

-- Add scheduling columns to tasks
ALTER TABLE tasks ADD COLUMN scheduled_start TIMESTAMP;
ALTER TABLE tasks ADD COLUMN scheduled_end TIMESTAMP;

-- Add type column to tasks (default 'TASK')
ALTER TABLE tasks ADD COLUMN type VARCHAR(20) DEFAULT 'TASK';

-- Add calendar token to user_settings
ALTER TABLE user_settings ADD COLUMN calendar_token VARCHAR(64);

-- Create task_dependencies table (Self-referencing ManyToMany)
CREATE TABLE task_dependencies (
    blocked_task_id VARCHAR(36) NOT NULL,
    blocker_task_id VARCHAR(36) NOT NULL,
    PRIMARY KEY (blocked_task_id, blocker_task_id),
    CONSTRAINT fk_td_blocked FOREIGN KEY (blocked_task_id) REFERENCES tasks(id) ON DELETE CASCADE,
    CONSTRAINT fk_td_blocker FOREIGN KEY (blocker_task_id) REFERENCES tasks(id) ON DELETE CASCADE
);

CREATE INDEX idx_task_dependencies_blocked ON task_dependencies(blocked_task_id);
CREATE INDEX idx_task_dependencies_blocker ON task_dependencies(blocker_task_id);
