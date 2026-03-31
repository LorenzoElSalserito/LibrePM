-- V51: Status reviews
CREATE TABLE IF NOT EXISTS status_reviews (
    id              VARCHAR(36) PRIMARY KEY,
    project_id      VARCHAR(36) NOT NULL,
    reviewer_id     VARCHAR(36) NOT NULL,
    review_date     DATE        NOT NULL,
    overall_status  VARCHAR(16) NOT NULL,
    schedule_status VARCHAR(16),
    budget_status   VARCHAR(16),
    risk_status     VARCHAR(16),
    summary         TEXT,
    achievements    TEXT,
    blockers        TEXT,
    next_actions    TEXT,
    created_at      TIMESTAMP   DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (project_id) REFERENCES projects(id),
    FOREIGN KEY (reviewer_id) REFERENCES users(id)
);
