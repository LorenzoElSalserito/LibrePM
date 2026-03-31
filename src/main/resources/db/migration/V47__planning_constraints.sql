-- V47: Planning constraints and critical path fields
ALTER TABLE tasks ADD COLUMN constraint_type VARCHAR(32);
ALTER TABLE tasks ADD COLUMN constraint_date DATE;
ALTER TABLE tasks ADD COLUMN early_start DATE;
ALTER TABLE tasks ADD COLUMN early_finish DATE;
ALTER TABLE tasks ADD COLUMN late_start DATE;
ALTER TABLE tasks ADD COLUMN late_finish DATE;
ALTER TABLE tasks ADD COLUMN total_float INTEGER;
ALTER TABLE tasks ADD COLUMN is_critical BOOLEAN DEFAULT FALSE;
