-- V6: Add Checklist Items

CREATE TABLE task_checklist_items (
    id VARCHAR(36) PRIMARY KEY,
    text VARCHAR(1000) NOT NULL,
    done BOOLEAN NOT NULL DEFAULT 0,
    sort_order INTEGER NOT NULL DEFAULT 0,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    task_id VARCHAR(36) NOT NULL,
    CONSTRAINT fk_checklist_task FOREIGN KEY (task_id) REFERENCES tasks(id) ON DELETE CASCADE
);

CREATE INDEX idx_checklist_item_task ON task_checklist_items(task_id);
