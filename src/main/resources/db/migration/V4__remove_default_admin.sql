-- ============================================
-- LibrePM Database Schema - Migration V4
-- Version: 0.3.1
-- Removes: Default admin user and sample data
-- ============================================

-- Rimuovi i task di esempio (collegati al progetto di esempio)
DELETE FROM tasks WHERE project_id = 'sample-project-001';

-- Rimuovi il progetto di esempio
DELETE FROM projects WHERE id = 'sample-project-001';

-- Rimuovi l'utente admin di default
DELETE FROM users WHERE id = 'default-user-001';

-- ============================================
-- Fine Migration V4
-- ============================================