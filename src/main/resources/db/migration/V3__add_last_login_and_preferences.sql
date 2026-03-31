-- ============================================
-- LibrePM Database Schema - Migration V3
-- Version: 0.3.0
-- Adds: last_login_at, app_preferences table
-- ============================================

-- Aggiungi colonna last_login_at alla tabella users
ALTER TABLE users ADD COLUMN last_login_at TEXT;

-- Tabella preferenze applicazione (per profilo/desktop)
-- Questa tabella è pensata per contenere preferenze globali dell'app
-- non legate a un utente specifico (come lastUserId e autologin)
CREATE TABLE IF NOT EXISTS app_preferences (
                                               key TEXT PRIMARY KEY,
                                               value TEXT NOT NULL,
                                               updated_at TEXT NOT NULL DEFAULT (datetime('now'))
    );

-- Inserisci preferenze di default
INSERT OR IGNORE INTO app_preferences (key, value, updated_at) VALUES
    ('autologin_enabled', 'false', datetime('now')),
    ('last_user_id', '', datetime('now'));

-- Aggiorna l'utente default con last_login_at
UPDATE users
SET last_login_at = datetime('now')
WHERE id = 'default-user-001';

-- ============================================
-- Fine Migration V3
-- ============================================