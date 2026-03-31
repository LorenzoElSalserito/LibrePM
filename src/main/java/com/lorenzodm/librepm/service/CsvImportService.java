package com.lorenzodm.librepm.service;

import com.lorenzodm.librepm.core.entity.ImportExportJob;

/**
 * CSV import service for tasks (PRD-14-FR-004, PRD-14-AC-003).
 * PRD-14-BR-002: import uses validation and staging before commit.
 * PRD-14-FR-009: jobs are traceable with outcome.
 */
public interface CsvImportService {

    /**
     * Validates and imports tasks from CSV into a project.
     * Pipeline: parse → validate rows → stage → commit valid rows.
     * Returns an ImportExportJob with status COMPLETED or FAILED.
     * PRD-14-BR-002: invalid rows are reported in errorMessage; valid rows are committed.
     */
    ImportExportJob importTasksCsv(String userId, String projectId, String csvContent);
}
