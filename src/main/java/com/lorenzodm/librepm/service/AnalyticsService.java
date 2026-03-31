package com.lorenzodm.librepm.service;

import com.lorenzodm.librepm.api.dto.response.AnalyticsResponse;
import com.lorenzodm.librepm.api.dto.response.FocusHeatmapResponse;
import com.lorenzodm.librepm.api.dto.response.FocusStatsResponse;

public interface AnalyticsService {
    AnalyticsResponse getEstimatesAnalytics(String userId, String projectId);
    FocusHeatmapResponse getFocusHeatmap(String userId, String projectId, int daysRange);
    FocusStatsResponse getFocusStats(String userId, String period);
}
