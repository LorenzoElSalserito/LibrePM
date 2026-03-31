-- V42: Event journal for local observability
CREATE TABLE IF NOT EXISTS event_journal (
    id          INTEGER PRIMARY KEY AUTOINCREMENT,
    event_type  VARCHAR(64)  NOT NULL,
    entity_type VARCHAR(64),
    entity_id   VARCHAR(36),
    payload     TEXT,
    user_id     VARCHAR(36),
    timestamp   TIMESTAMP    DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_event_journal_time ON event_journal(timestamp);
CREATE INDEX IF NOT EXISTS idx_event_journal_type ON event_journal(event_type);
