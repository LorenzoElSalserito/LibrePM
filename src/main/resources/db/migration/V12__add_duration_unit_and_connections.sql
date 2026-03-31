-- Aggiunge la colonna duration_unit alla tabella tasks
ALTER TABLE tasks ADD COLUMN duration_unit VARCHAR(20) DEFAULT 'MINUTES';

-- Crea la tabella user_connections per la gestione delle amicizie
CREATE TABLE user_connections (
    id VARCHAR(36) PRIMARY KEY,
    requester_id VARCHAR(36) NOT NULL,
    target_id VARCHAR(36) NOT NULL,
    status VARCHAR(20) NOT NULL,
    created_at DATETIME NOT NULL,
    updated_at DATETIME,
    FOREIGN KEY (requester_id) REFERENCES users(id),
    FOREIGN KEY (target_id) REFERENCES users(id),
    UNIQUE(requester_id, target_id)
);

-- Indici per performance
CREATE INDEX idx_connections_requester ON user_connections(requester_id);
CREATE INDEX idx_connections_target ON user_connections(target_id);
CREATE INDEX idx_connections_status ON user_connections(status);
