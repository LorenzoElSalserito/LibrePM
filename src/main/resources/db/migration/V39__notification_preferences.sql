-- V39: Notification preferences (PRD-04-FR-004)
CREATE TABLE IF NOT EXISTS notification_preferences (
    id TEXT PRIMARY KEY,
    user_id TEXT NOT NULL,
    event_type TEXT NOT NULL,
    channel TEXT NOT NULL DEFAULT 'IN_APP',
    enabled INTEGER DEFAULT 1,
    severity_threshold TEXT DEFAULT 'INFO',
    created_at TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id),
    UNIQUE(user_id, event_type, channel)
);

CREATE INDEX IF NOT EXISTS idx_notif_pref_user ON notification_preferences(user_id);
