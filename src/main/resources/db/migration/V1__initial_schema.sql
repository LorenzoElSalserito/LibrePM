-- ============================================
-- LibrePM Database Schema - Initial Migration
-- Version: 1.0.0
-- Database: SQLite
-- ============================================

-- Users table
CREATE TABLE users (
                       id TEXT PRIMARY KEY,
                       username TEXT NOT NULL UNIQUE,
                       email TEXT UNIQUE,
                       password_hash TEXT NOT NULL,
                       display_name TEXT,
                       avatar_path TEXT,
                       active INTEGER NOT NULL DEFAULT 1,
                       created_at TEXT NOT NULL,
                       updated_at TEXT NOT NULL,
                       last_synced_at TEXT,
                       sync_status TEXT DEFAULT 'LOCAL_ONLY'
);

CREATE INDEX idx_username ON users(username);
CREATE INDEX idx_email ON users(email);

-- Projects table
CREATE TABLE projects (
                          id TEXT PRIMARY KEY,
                          name TEXT NOT NULL,
                          description TEXT,
                          color TEXT,
                          icon TEXT,
                          archived INTEGER NOT NULL DEFAULT 0,
                          favorite INTEGER NOT NULL DEFAULT 0,
                          created_at TEXT NOT NULL,
                          updated_at TEXT NOT NULL,
                          last_synced_at TEXT,
                          sync_status TEXT DEFAULT 'LOCAL_ONLY',
                          owner_id TEXT NOT NULL,
                          FOREIGN KEY (owner_id) REFERENCES users(id) ON DELETE CASCADE
);

CREATE INDEX idx_project_owner ON projects(owner_id);
CREATE INDEX idx_project_archived ON projects(archived);
CREATE INDEX idx_project_created ON projects(created_at);

-- Tasks table
CREATE TABLE tasks (
                       id TEXT PRIMARY KEY,
                       title TEXT NOT NULL,
                       description TEXT,
                       status TEXT NOT NULL DEFAULT 'TODO',
                       priority TEXT NOT NULL DEFAULT 'MED',
                       deadline TEXT,
                       owner TEXT,
                       notes TEXT,
                       asset_path TEXT,
                       asset_file_name TEXT,
                       asset_mime_type TEXT,
                       asset_size_bytes INTEGER,
                       archived INTEGER NOT NULL DEFAULT 0,
                       sort_order INTEGER NOT NULL DEFAULT 0,
                       created_at TEXT NOT NULL,
                       updated_at TEXT NOT NULL,
                       last_synced_at TEXT,
                       sync_status TEXT DEFAULT 'LOCAL_ONLY',
                       project_id TEXT NOT NULL,
                       assigned_to_id TEXT,
                       FOREIGN KEY (project_id) REFERENCES projects(id) ON DELETE CASCADE,
                       FOREIGN KEY (assigned_to_id) REFERENCES users(id) ON DELETE SET NULL
);

CREATE INDEX idx_task_project ON tasks(project_id);
CREATE INDEX idx_task_assigned ON tasks(assigned_to_id);
CREATE INDEX idx_task_status ON tasks(status);
CREATE INDEX idx_task_priority ON tasks(priority);
CREATE INDEX idx_task_deadline ON tasks(deadline);
CREATE INDEX idx_task_created ON tasks(created_at);

-- Focus Sessions table
CREATE TABLE focus_sessions (
                                id TEXT PRIMARY KEY,
                                started_at TEXT NOT NULL,
                                ended_at TEXT,
                                duration_ms INTEGER NOT NULL DEFAULT 0,
                                notes TEXT,
                                session_type TEXT DEFAULT 'FOCUS',
                                created_at TEXT NOT NULL,
                                last_synced_at TEXT,
                                sync_status TEXT DEFAULT 'LOCAL_ONLY',
                                task_id TEXT NOT NULL,
                                user_id TEXT NOT NULL,
                                FOREIGN KEY (task_id) REFERENCES tasks(id) ON DELETE CASCADE,
                                FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

CREATE INDEX idx_focus_task ON focus_sessions(task_id);
CREATE INDEX idx_focus_user ON focus_sessions(user_id);
CREATE INDEX idx_focus_started ON focus_sessions(started_at);
CREATE INDEX idx_focus_ended ON focus_sessions(ended_at);

-- Assets table
CREATE TABLE assets (
                        id TEXT PRIMARY KEY,
                        file_name TEXT NOT NULL,
                        file_path TEXT NOT NULL UNIQUE,
                        mime_type TEXT,
                        size_bytes INTEGER NOT NULL,
                        checksum TEXT,
                        description TEXT,
                        thumbnail_path TEXT,
                        deleted INTEGER NOT NULL DEFAULT 0,
                        created_at TEXT NOT NULL,
                        last_accessed_at TEXT,
                        last_synced_at TEXT,
                        sync_status TEXT DEFAULT 'LOCAL_ONLY',
                        cloud_url TEXT,
                        owner_id TEXT NOT NULL,
                        FOREIGN KEY (owner_id) REFERENCES users(id) ON DELETE CASCADE
);

CREATE INDEX idx_asset_owner ON assets(owner_id);
CREATE INDEX idx_asset_created ON assets(created_at);
CREATE INDEX idx_asset_type ON assets(mime_type);

-- ============================================
-- NOTA: Nessun dato di default viene inserito.
-- L'utente deve creare il proprio profilo al primo avvio
-- tramite l'interfaccia di onboarding.
-- ============================================

-- ============================================
-- Fine initial schema
-- ============================================