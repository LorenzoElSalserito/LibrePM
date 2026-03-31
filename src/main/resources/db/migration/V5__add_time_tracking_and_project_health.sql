-- V5: Add Time Tracking fields to Tasks and Project Health fields

-- Add time tracking columns to tasks
ALTER TABLE tasks ADD COLUMN estimated_minutes INTEGER;
ALTER TABLE tasks ADD COLUMN actual_minutes INTEGER DEFAULT 0;

-- Add project health monitoring fields
ALTER TABLE projects ADD COLUMN overdue_warning_threshold INTEGER DEFAULT 3;
ALTER TABLE projects ADD COLUMN overdue_count INTEGER DEFAULT 0;
ALTER TABLE projects ADD COLUMN health_status VARCHAR(20) DEFAULT 'OK';
