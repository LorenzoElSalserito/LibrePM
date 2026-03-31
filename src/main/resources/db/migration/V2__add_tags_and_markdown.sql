-- ============================================
-- LibrePM - Migration V2
-- Aggiunta funzionalità:
-- - Tag per task
-- - Note markdown separate
-- - Asset multipli per task
-- - Reminder e notifiche
-- ============================================

-- Tabella Tag
CREATE TABLE IF NOT EXISTS tags (
    id TEXT PRIMARY KEY,
    name TEXT NOT NULL UNIQUE,
    color TEXT,
    created_at TEXT NOT NULL,
    owner_id TEXT NOT NULL,
    FOREIGN KEY (owner_id) REFERENCES users(id) ON DELETE CASCADE
);

CREATE INDEX IF NOT EXISTS idx_tag_name ON tags(name);
CREATE INDEX IF NOT EXISTS idx_tag_owner ON tags(owner_id);

-- Tabella relazione Task-Tag (many-to-many)
CREATE TABLE IF NOT EXISTS task_tags (
    task_id TEXT NOT NULL,
    tag_id TEXT NOT NULL,
    created_at TEXT NOT NULL,
    PRIMARY KEY (task_id, tag_id),
    FOREIGN KEY (task_id) REFERENCES tasks(id) ON DELETE CASCADE,
    FOREIGN KEY (tag_id) REFERENCES tags(id) ON DELETE CASCADE
);

CREATE INDEX IF NOT EXISTS idx_task_tags_task ON task_tags(task_id);
CREATE INDEX IF NOT EXISTS idx_task_tags_tag ON task_tags(tag_id);

-- Aggiungi campo markdown_notes a tasks
ALTER TABLE tasks ADD COLUMN markdown_notes TEXT;

-- Aggiungi campi per reminder/notifiche
ALTER TABLE tasks ADD COLUMN reminder_date TEXT;
ALTER TABLE tasks ADD COLUMN reminder_enabled INTEGER NOT NULL DEFAULT 0;
ALTER TABLE tasks ADD COLUMN notification_sent INTEGER NOT NULL DEFAULT 0;

-- Modifica tabella assets per supporto multiplo per task
-- Aggiungi colonna task_id per relazione con task
ALTER TABLE assets ADD COLUMN task_id TEXT;

-- Rimuoviamo i vecchi campi asset da tasks (saranno gestiti dalla tabella assets)
-- Non possiamo fare DROP COLUMN in SQLite, quindi li lasciamo per compatibilità
-- I nuovi task useranno solo la tabella assets

-- Aggiungi indice per asset_path e task_id
CREATE INDEX IF NOT EXISTS idx_asset_task ON assets(task_id);

-- Tabella per collaboratori progetto (per feature collaborative)
CREATE TABLE IF NOT EXISTS project_collaborators (
    id TEXT PRIMARY KEY,
    project_id TEXT NOT NULL,
    user_id TEXT NOT NULL,
    role TEXT NOT NULL DEFAULT 'VIEWER', -- OWNER, EDITOR, VIEWER
    invited_at TEXT NOT NULL,
    accepted_at TEXT,
    FOREIGN KEY (project_id) REFERENCES projects(id) ON DELETE CASCADE,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    UNIQUE(project_id, user_id)
);

CREATE INDEX IF NOT EXISTS idx_project_collab_project ON project_collaborators(project_id);
CREATE INDEX IF NOT EXISTS idx_project_collab_user ON project_collaborators(user_id);

-- Tabella per impostazioni utente
CREATE TABLE IF NOT EXISTS user_settings (
    user_id TEXT PRIMARY KEY,
    theme TEXT DEFAULT 'dark',
    language TEXT DEFAULT 'it',
    notifications_enabled INTEGER NOT NULL DEFAULT 1,
    focus_timer_default_minutes INTEGER NOT NULL DEFAULT 25,
    auto_backup_enabled INTEGER NOT NULL DEFAULT 1,
    last_backup_at TEXT,
    created_at TEXT NOT NULL,
    updated_at TEXT NOT NULL,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

-- Inserisci settings di default per utente esistente
INSERT OR IGNORE INTO user_settings (
    user_id,
    theme,
    language,
    notifications_enabled,
    focus_timer_default_minutes,
    auto_backup_enabled,
    created_at,
    updated_at
) VALUES (
    'default-user-001',
    'dark',
    'it',
    1,
    25,
    1,
    datetime('now'),
    datetime('now')
);

-- Tag di esempio
INSERT OR IGNORE INTO tags (id, name, color, created_at, owner_id)
VALUES 
    ('tag-urgent', 'Urgente', '#dc3545', datetime('now'), 'default-user-001'),
    ('tag-important', 'Importante', '#fd7e14', datetime('now'), 'default-user-001'),
    ('tag-review', 'Da rivedere', '#0dcaf0', datetime('now'), 'default-user-001'),
    ('tag-waiting', 'In attesa', '#6c757d', datetime('now'), 'default-user-001');

-- ============================================
-- Fine Migration V2
-- ============================================
