-- V49: Capacity profiles and leave records
CREATE TABLE IF NOT EXISTS capacity_profiles (
    id              VARCHAR(36) PRIMARY KEY,
    user_id         VARCHAR(36) NOT NULL,
    calendar_id     VARCHAR(36),
    hours_per_day   REAL        DEFAULT 8.0,
    effective_from  DATE        NOT NULL,
    effective_to    DATE,
    FOREIGN KEY (user_id) REFERENCES users(id),
    FOREIGN KEY (calendar_id) REFERENCES work_calendars(id)
);

CREATE TABLE IF NOT EXISTS leave_records (
    id          VARCHAR(36) PRIMARY KEY,
    user_id     VARCHAR(36) NOT NULL,
    leave_date  DATE        NOT NULL,
    leave_type  VARCHAR(32),
    hours       REAL,
    description TEXT,
    FOREIGN KEY (user_id) REFERENCES users(id),
    UNIQUE(user_id, leave_date)
);

CREATE INDEX IF NOT EXISTS idx_leave_records ON leave_records(user_id, leave_date);
