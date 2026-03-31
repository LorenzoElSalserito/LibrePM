package com.lorenzodm.librepm.api.controller;

import com.lorenzodm.librepm.api.dto.response.AnalyticsResponse;
import com.lorenzodm.librepm.api.dto.response.FocusHeatmapResponse;
import com.lorenzodm.librepm.api.dto.response.FocusStatsResponse;
import com.lorenzodm.librepm.service.AnalyticsService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users/{userId}/analytics")
public class AnalyticsController {

    private final AnalyticsService analyticsService;

    public AnalyticsController(AnalyticsService analyticsService) {
        this.analyticsService = analyticsService;
    }

    @GetMapping("/estimates")
    public ResponseEntity<AnalyticsResponse> getEstimatesAnalytics(
            @PathVariable String userId,
            @RequestParam(required = false) String projectId
    ) {
        return ResponseEntity.ok(analyticsService.getEstimatesAnalytics(userId, projectId));
    }

    @GetMapping("/focus-heatmap")
    public ResponseEntity<FocusHeatmapResponse> getFocusHeatmap(
            @PathVariable String userId,
            @RequestParam(required = false) String projectId,
            @RequestParam(defaultValue = "365") int range
    ) {
        return ResponseEntity.ok(analyticsService.getFocusHeatmap(userId, projectId, range));
    }

    @GetMapping("/focus-stats")
    public ResponseEntity<FocusStatsResponse> getFocusStats(
            @PathVariable String userId,
            @RequestParam(defaultValue = "week") String period
    ) {
        return ResponseEntity.ok(analyticsService.getFocusStats(userId, period));
    }
}
