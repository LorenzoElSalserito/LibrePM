package com.lorenzodm.librepm.service;

import com.lorenzodm.librepm.api.dto.response.VarianceResponse;

/**
 * Calculates variance between a baseline and the current project plan (PRD-11).
 * PRD-11-BR-004: distinguishes schedule variance and effort variance.
 */
public interface VarianceService {

    /**
     * Computes the full variance report for a specific baseline vs current plan.
     */
    VarianceResponse calculateVariance(String userId, String projectId, String baselineId);

    /**
     * Computes variance against the most recent baseline (convenience method).
     */
    VarianceResponse calculateLatestVariance(String userId, String projectId);
}
