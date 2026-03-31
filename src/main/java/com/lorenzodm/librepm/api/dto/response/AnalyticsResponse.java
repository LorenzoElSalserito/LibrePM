package com.lorenzodm.librepm.api.dto.response;

import java.util.List;

public record AnalyticsResponse(
        Double globalDeviationPercentage, // Media deviazione globale
        List<ProjectAnalyticsResponse> projectAnalytics, // Deviazione per progetto
        List<TaskDeviationResponse> topUnderestimatedTasks, // Top 5 task sottostimati
        List<TaskDeviationResponse> topOverestimatedTasks // Top 5 task sovrastimati
) {}
