package com.lorenzodm.librepm.service;

import com.lorenzodm.librepm.api.dto.request.CreateBaselineRequest;
import com.lorenzodm.librepm.core.entity.Baseline;

import java.util.List;

public interface BaselineService {

    /**
     * Creates an immutable snapshot of the current project plan (PRD-11-FR-001).
     * Snapshots all tasks with their current plannedStart, plannedFinish, estimatedEffort.
     */
    Baseline create(String userId, String projectId, CreateBaselineRequest req);

    Baseline getById(String userId, String projectId, String baselineId);

    List<Baseline> listByProject(String userId, String projectId);

    void delete(String userId, String projectId, String baselineId);

    /**
     * Compares a baseline snapshot with the current project state.
     */
    java.util.Map<String, Object> compare(String userId, String projectId, String baselineId);
}
