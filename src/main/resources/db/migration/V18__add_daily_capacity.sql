-- V18: Add daily work capacity to user settings
-- Author: Lorenzo DM
-- Date: 2026-01-30

ALTER TABLE user_settings ADD COLUMN daily_work_capacity_minutes INTEGER DEFAULT 480 NOT NULL;
