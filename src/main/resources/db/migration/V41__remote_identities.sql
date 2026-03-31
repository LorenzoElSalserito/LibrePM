-- V41: Remote identities for OIDC/OAuth2 preparation
CREATE TABLE IF NOT EXISTS remote_identities (
    id            VARCHAR(36)  PRIMARY KEY,
    user_id       VARCHAR(36)  NOT NULL,
    provider      VARCHAR(64)  NOT NULL,  -- GOOGLE, MICROSOFT, KEYCLOAK, GENERIC_OIDC
    provider_user_id VARCHAR(255) NOT NULL,
    email         VARCHAR(255),
    bound_at      TIMESTAMP    DEFAULT CURRENT_TIMESTAMP,
    revoked_at    TIMESTAMP,
    deleted       BOOLEAN      DEFAULT FALSE,
    version       BIGINT       DEFAULT 0,
    FOREIGN KEY (user_id) REFERENCES users(id),
    UNIQUE(provider, provider_user_id)
);
