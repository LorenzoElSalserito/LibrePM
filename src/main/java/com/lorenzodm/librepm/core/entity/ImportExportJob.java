package com.lorenzodm.librepm.core.entity;

import jakarta.persistence.*;
import java.time.Instant;

/**
 * Tracks import and export jobs with status and result (PRD-14-FR-009).
 * Jobs are traceable and their outcome is persisted.
 *
 * @author Lorenzo DM
 * @since 1.0.0
 */
@Entity
@Table(name = "import_export_jobs", indexes = {
        @Index(name = "idx_job_user", columnList = "user_id"),
        @Index(name = "idx_job_project", columnList = "project_id"),
        @Index(name = "idx_job_status", columnList = "status")
})
public class ImportExportJob extends BaseSyncEntity {

    public enum JobType {
        EXPORT_CSV,
        EXPORT_JSON,
        EXPORT_CHARTER_JSON,
        IMPORT_CSV_TASKS,
        EXPORT_ICS_SNAPSHOT
    }

    public enum JobStatus {
        PENDING,
        PROCESSING,
        COMPLETED,
        FAILED
    }

    @Column(nullable = false, length = 36)
    private String userId;

    /** Optional: scope to a single project. */
    @Column(length = 36)
    private String projectId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private JobType jobType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private JobStatus status = JobStatus.PENDING;

    /** For export jobs: path/key of the produced file or inline content reference. */
    @Column(columnDefinition = "TEXT")
    private String resultPayload;

    /** Error message if the job failed. */
    @Column(columnDefinition = "TEXT")
    private String errorMessage;

    /** Number of records processed (rows imported / tasks exported). */
    @Column
    private Integer recordCount;

    @Column
    private Instant completedAt;

    public ImportExportJob() {
        super();
    }

    // Getters and Setters
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }
    public String getProjectId() { return projectId; }
    public void setProjectId(String projectId) { this.projectId = projectId; }
    public JobType getJobType() { return jobType; }
    public void setJobType(JobType jobType) { this.jobType = jobType; }
    public JobStatus getStatus() { return status; }
    public void setStatus(JobStatus status) { this.status = status; }
    public String getResultPayload() { return resultPayload; }
    public void setResultPayload(String resultPayload) { this.resultPayload = resultPayload; }
    public String getErrorMessage() { return errorMessage; }
    public void setErrorMessage(String errorMessage) { this.errorMessage = errorMessage; }
    public Integer getRecordCount() { return recordCount; }
    public void setRecordCount(Integer recordCount) { this.recordCount = recordCount; }
    public Instant getCompletedAt() { return completedAt; }
    public void setCompletedAt(Instant completedAt) { this.completedAt = completedAt; }
}
