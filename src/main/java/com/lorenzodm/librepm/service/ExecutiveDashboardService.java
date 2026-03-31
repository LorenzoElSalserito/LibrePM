package com.lorenzodm.librepm.service;

import com.lorenzodm.librepm.api.dto.response.ExecutiveDashboardResponse;

/**
 * Executive dashboard aggregation service (PRD-17).
 * Combines charter, deliverables, risks, OKRs, variance, and workload into a single view.
 */
public interface ExecutiveDashboardService {

    /**
     * Returns the full executive dashboard for a project.
     * PRD-17-FR-001: single-view aggregation.
     */
    ExecutiveDashboardResponse getDashboard(String userId, String projectId);
}
