-- V34: FTS5 full-text search for notes and tasks (PRD-02-FR-004)

-- Notes FTS5 index
CREATE VIRTUAL TABLE IF NOT EXISTS notes_fts USING fts5(title, content, content=notes, content_rowid=rowid);

-- Populate FTS index from existing data
INSERT INTO notes_fts(rowid, title, content)
    SELECT rowid, title, content FROM notes WHERE deleted_at IS NULL;

-- Triggers to keep FTS index synchronized
CREATE TRIGGER notes_fts_insert AFTER INSERT ON notes BEGIN
    INSERT INTO notes_fts(rowid, title, content) VALUES (new.rowid, new.title, new.content);
END;

CREATE TRIGGER notes_fts_delete AFTER DELETE ON notes BEGIN
    INSERT INTO notes_fts(notes_fts, rowid, title, content) VALUES('delete', old.rowid, old.title, old.content);
END;

CREATE TRIGGER notes_fts_update AFTER UPDATE ON notes BEGIN
    INSERT INTO notes_fts(notes_fts, rowid, title, content) VALUES('delete', old.rowid, old.title, old.content);
    INSERT INTO notes_fts(rowid, title, content) VALUES (new.rowid, new.title, new.content);
END;

-- Tasks FTS5 index
CREATE VIRTUAL TABLE IF NOT EXISTS tasks_fts USING fts5(title, description, content=tasks, content_rowid=rowid);

-- Populate FTS index from existing data
INSERT INTO tasks_fts(rowid, title, description)
    SELECT rowid, title, description FROM tasks WHERE deleted_at IS NULL;

-- Triggers to keep FTS index synchronized
CREATE TRIGGER tasks_fts_insert AFTER INSERT ON tasks BEGIN
    INSERT INTO tasks_fts(rowid, title, description) VALUES (new.rowid, new.title, new.description);
END;

CREATE TRIGGER tasks_fts_delete AFTER DELETE ON tasks BEGIN
    INSERT INTO tasks_fts(tasks_fts, rowid, title, description) VALUES('delete', old.rowid, old.title, old.description);
END;

CREATE TRIGGER tasks_fts_update AFTER UPDATE ON tasks BEGIN
    INSERT INTO tasks_fts(tasks_fts, rowid, title, description) VALUES('delete', old.rowid, old.title, old.description);
    INSERT INTO tasks_fts(rowid, title, description) VALUES (new.rowid, new.title, new.description);
END;
