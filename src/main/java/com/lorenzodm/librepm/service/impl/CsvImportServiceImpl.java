package com.lorenzodm.librepm.service.impl;

import com.lorenzodm.librepm.api.exception.ResourceNotFoundException;
import com.lorenzodm.librepm.core.entity.*;
import com.lorenzodm.librepm.repository.*;
import com.lorenzodm.librepm.service.CsvImportService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.BufferedReader;
import java.io.StringReader;
import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * CSV import for tasks (PRD-14-FR-004, PRD-14-AC-003).
 * PRD-14-BR-002: parse → validate → stage → commit valid rows only.
 * Invalid rows are reported in errorMessage; valid rows are committed.
 */
@Service
@Transactional
public class CsvImportServiceImpl implements CsvImportService {

    private final ProjectRepository projectRepository;
    private final TaskRepository taskRepository;
    private final TaskStatusRepository taskStatusRepository;
    private final TaskPriorityRepository taskPriorityRepository;
    private final ImportExportJobRepository jobRepository;

    public CsvImportServiceImpl(ProjectRepository projectRepository,
                                 TaskRepository taskRepository,
                                 TaskStatusRepository taskStatusRepository,
                                 TaskPriorityRepository taskPriorityRepository,
                                 ImportExportJobRepository jobRepository) {
        this.projectRepository = projectRepository;
        this.taskRepository = taskRepository;
        this.taskStatusRepository = taskStatusRepository;
        this.taskPriorityRepository = taskPriorityRepository;
        this.jobRepository = jobRepository;
    }

    @Override
    public ImportExportJob importTasksCsv(String userId, String projectId, String csvContent) {
        ImportExportJob job = new ImportExportJob();
        job.setUserId(userId);
        job.setProjectId(projectId);
        job.setJobType(ImportExportJob.JobType.IMPORT_CSV_TASKS);
        job.setStatus(ImportExportJob.JobStatus.PROCESSING);
        job = jobRepository.save(job);

        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ResourceNotFoundException("Progetto non trovato: " + projectId));

        List<String> errors = new ArrayList<>();
        List<Task> staged = new ArrayList<>();

        // PRD-14-BR-002: parse and validate before commit
        try (BufferedReader reader = new BufferedReader(new StringReader(csvContent))) {
            String headerLine = reader.readLine();
            if (headerLine == null) {
                return failJob(job, "File CSV vuoto");
            }

            String[] headers = parseCsvLine(headerLine);
            int titleIdx = findIndex(headers, "title");
            if (titleIdx < 0) {
                return failJob(job, "Colonna 'title' obbligatoria non trovata");
            }
            int descIdx    = findIndex(headers, "description");
            int statusIdx  = findIndex(headers, "status");
            int priorityIdx = findIndex(headers, "priority");
            int deadlineIdx = findIndex(headers, "deadline");
            int estimIdx   = findIndex(headers, "estimatedEffort");

            String line;
            int rowNum = 1;
            while ((line = reader.readLine()) != null) {
                rowNum++;
                if (line.isBlank()) continue;
                String[] cols = parseCsvLine(line);

                String title = safeGet(cols, titleIdx);
                if (title == null || title.isBlank()) {
                    errors.add("Riga " + rowNum + ": 'title' obbligatorio");
                    continue;
                }

                Task task = new Task();
                task.setProject(project);
                task.setTitle(title);

                if (descIdx >= 0) task.setDescription(safeGet(cols, descIdx));

                // Status lookup
                String statusName = safeGet(cols, statusIdx);
                if (statusName != null && !statusName.isBlank()) {
                    final String finalStatusName = statusName;
                    final int finalRowNum = rowNum;
                    taskStatusRepository.findByNameIgnoreCase(statusName)
                            .ifPresentOrElse(task::setStatus,
                                    () -> errors.add("Riga " + finalRowNum + ": status '" + finalStatusName + "' non trovato (ignorato)"));
                }
                // Default to TODO if no status resolved
                if (task.getStatus() == null) {
                    taskStatusRepository.findByNameIgnoreCase("TODO").ifPresent(task::setStatus);
                }

                // Priority lookup
                String priorityName = safeGet(cols, priorityIdx);
                if (priorityName != null && !priorityName.isBlank()) {
                    taskPriorityRepository.findByNameIgnoreCase(priorityName)
                            .ifPresent(task::setPriority);
                }

                // Deadline
                String deadlineStr = safeGet(cols, deadlineIdx);
                if (deadlineStr != null && !deadlineStr.isBlank()) {
                    try {
                        task.setDeadline(LocalDate.parse(deadlineStr));
                    } catch (Exception e) {
                        errors.add("Riga " + rowNum + ": deadline '" + deadlineStr + "' non parsabile (formato: YYYY-MM-DD)");
                    }
                }

                // Estimated effort
                String estimStr = safeGet(cols, estimIdx);
                if (estimStr != null && !estimStr.isBlank()) {
                    try {
                        task.setEstimatedEffort(Integer.parseInt(estimStr.trim()));
                    } catch (NumberFormatException e) {
                        errors.add("Riga " + rowNum + ": estimatedEffort '" + estimStr + "' non numerico (ignorato)");
                    }
                }

                staged.add(task);
            }

            // Commit all staged (valid) tasks
            taskRepository.saveAll(staged);

            job.setStatus(ImportExportJob.JobStatus.COMPLETED);
            job.setRecordCount(staged.size());
            job.setCompletedAt(Instant.now());
            if (!errors.isEmpty()) {
                job.setErrorMessage("Righe con warning: " + String.join("; ", errors));
            }
            return jobRepository.save(job);

        } catch (Exception e) {
            return failJob(job, "Errore lettura CSV: " + e.getMessage());
        }
    }

    // --- Private helpers ---

    private ImportExportJob failJob(ImportExportJob job, String error) {
        job.setStatus(ImportExportJob.JobStatus.FAILED);
        job.setErrorMessage(error);
        job.setCompletedAt(Instant.now());
        return jobRepository.save(job);
    }

    private String[] parseCsvLine(String line) {
        // Simple CSV parser (handles quoted fields with commas)
        List<String> result = new ArrayList<>();
        boolean inQuotes = false;
        StringBuilder current = new StringBuilder();
        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);
            if (c == '"') {
                if (inQuotes && i + 1 < line.length() && line.charAt(i + 1) == '"') {
                    current.append('"');
                    i++; // skip next quote
                } else {
                    inQuotes = !inQuotes;
                }
            } else if (c == ',' && !inQuotes) {
                result.add(current.toString().trim());
                current = new StringBuilder();
            } else {
                current.append(c);
            }
        }
        result.add(current.toString().trim());
        return result.toArray(new String[0]);
    }

    private int findIndex(String[] headers, String name) {
        for (int i = 0; i < headers.length; i++) {
            if (headers[i].trim().equalsIgnoreCase(name)) return i;
        }
        return -1;
    }

    private String safeGet(String[] cols, int idx) {
        if (idx < 0 || idx >= cols.length) return null;
        String val = cols[idx];
        return (val == null || val.isBlank()) ? null : val;
    }
}
