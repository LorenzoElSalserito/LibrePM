package com.lorenzodm.librepm.api.dto.response;

public record ProjectAnalyticsResponse(
        String projectId,
        String projectName,
        Double deviationPercentage,
        Integer totalTasksWithEstimates,
        Integer totalEstimatedMinutes,
        Integer totalActualMinutes
) {}
