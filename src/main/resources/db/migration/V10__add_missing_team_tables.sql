-- V10: Add Missing Team Tables and Columns

-- Create teams table
CREATE TABLE teams (
    id VARCHAR(36) PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    description VARCHAR(500),
    owner_id VARCHAR(36) NOT NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    CONSTRAINT fk_team_owner FOREIGN KEY (owner_id) REFERENCES users(id) ON DELETE CASCADE
);
CREATE INDEX idx_team_owner ON teams(owner_id);

-- Create team members table
CREATE TABLE team_members (
    team_id VARCHAR(36) NOT NULL,
    user_id VARCHAR(36) NOT NULL,
    role VARCHAR(20) NOT NULL,
    joined_at TIMESTAMP NOT NULL,
    PRIMARY KEY (team_id, user_id),
    CONSTRAINT fk_tm_team FOREIGN KEY (team_id) REFERENCES teams(id) ON DELETE CASCADE,
    CONSTRAINT fk_tm_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

-- Add team_id and visibility to projects
ALTER TABLE projects ADD COLUMN team_id VARCHAR(36);
ALTER TABLE projects ADD COLUMN visibility VARCHAR(20) DEFAULT 'PERSONAL';
CREATE INDEX idx_project_team ON projects(team_id);
