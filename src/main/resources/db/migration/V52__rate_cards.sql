-- V52: Rate cards for costed capacity
CREATE TABLE IF NOT EXISTS rate_cards (
    id              VARCHAR(36) PRIMARY KEY,
    scope           VARCHAR(32) NOT NULL,
    scope_entity_id VARCHAR(36) NOT NULL,
    currency        VARCHAR(3)  DEFAULT 'EUR',
    hourly_rate     REAL        NOT NULL,
    daily_rate      REAL,
    effective_from  DATE        NOT NULL,
    effective_to    DATE,
    created_at      TIMESTAMP   DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_rate_cards_scope ON rate_cards(scope, scope_entity_id, effective_from);
