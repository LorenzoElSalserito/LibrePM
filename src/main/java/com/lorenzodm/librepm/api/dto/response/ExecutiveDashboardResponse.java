package com.lorenzodm.librepm.api.dto.response;

import java.util.List;

/**
 * Aggregated executive dashboard for a project (PRD-17).
 * Combines charter, risks, deliverables, OKRs and project status.
 */
public record ExecutiveDashboardResponse(
        String projectId,
        String projectName,
        String projectStatus, // ON_TRACK, AT_RISK, DELAYED, BLOCKED

        // PRD-17 Charter
        ProjectCharterResponse charter,

        // PRD-17 Key Deliverables
        List<DeliverableResponse> deliverables,
        int completedDeliverables,
        int totalDeliverables,

        // PRD-17 Risk Register
        List<RiskRegisterEntryResponse> risks,
        int highImpactRisks,

        // PRD-17 OKRs
        List<OkrResponse> okrs,

        // PRD-11 Latest Baseline Variance
        VarianceResponse latestVariance,

        // PRD-12 Workload summary (overbooked users count)
        int overbookedUsersCount,

        // Synthetic metrics
        double completionPercentage,  // from tasks
        long overdueTaskCount
) {}
