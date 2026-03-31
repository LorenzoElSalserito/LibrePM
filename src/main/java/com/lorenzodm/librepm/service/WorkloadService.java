package com.lorenzodm.librepm.service;

import com.lorenzodm.librepm.api.dto.response.WorkloadSliceResponse;

import java.time.LocalDate;
import java.util.List;

/**
 * Workload view service (PRD-12).
 * Calculates capacity vs workload for resources, detecting overbooking.
 */
public interface WorkloadService {

    /**
     * Returns workload slices for all users in a project for the given period.
     * PRD-12-FR-004: workload for day, week, interval.
     * PRD-12-FR-007: detect overbooking.
     */
    List<WorkloadSliceResponse> getProjectWorkload(String userId, String projectId,
                                                    LocalDate periodStart, LocalDate periodEnd);

    /**
     * Returns workload slice for a single user across all projects.
     */
    WorkloadSliceResponse getUserWorkload(String userId, LocalDate periodStart, LocalDate periodEnd);
}
