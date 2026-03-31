-- ============================================
-- V27: Fix work_day_rules time format
-- Hibernate SQLite dialect expects HH:mm:ss but V22 seeded HH:mm.
-- ============================================

UPDATE work_day_rules SET start_time = start_time || ':00'
    WHERE start_time IS NOT NULL AND length(start_time) = 5;

UPDATE work_day_rules SET end_time = end_time || ':00'
    WHERE end_time IS NOT NULL AND length(end_time) = 5;

UPDATE work_day_rules SET break_start_time = break_start_time || ':00'
    WHERE break_start_time IS NOT NULL AND length(break_start_time) = 5;

UPDATE work_day_rules SET break_end_time = break_end_time || ':00'
    WHERE break_end_time IS NOT NULL AND length(break_end_time) = 5;
