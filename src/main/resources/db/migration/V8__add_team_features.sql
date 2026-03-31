-- V8: Add Team Features (Project Members and Ghost Users)

-- Add ghost user fields
ALTER TABLE users ADD COLUMN is_ghost BOOLEAN NOT NULL DEFAULT 0;
ALTER TABLE users ADD COLUMN created_by_id VARCHAR(36);
CREATE INDEX idx_user_created_by ON users(created_by_id);

-- Create project members table
CREATE TABLE project_members (
    project_id VARCHAR(36) NOT NULL,
    user_id VARCHAR(36) NOT NULL,
    role VARCHAR(20) NOT NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    PRIMARY KEY (project_id, user_id),
    CONSTRAINT fk_pm_project FOREIGN KEY (project_id) REFERENCES projects(id) ON DELETE CASCADE,
    CONSTRAINT fk_pm_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

-- Migrate existing owners as members
INSERT INTO project_members (project_id, user_id, role, created_at, updated_at)
SELECT id, owner_id, 'OWNER', created_at, updated_at FROM projects;
