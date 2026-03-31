package com.lorenzodm.librepm.api.controller;

import com.lorenzodm.librepm.api.dto.response.ExecutiveDashboardResponse;
import com.lorenzodm.librepm.service.ExecutiveDashboardService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Executive dashboard endpoint (PRD-17).
 * Returns the full aggregated project view for executives/sponsors.
 */
@RestController
@RequestMapping("/api/users/{userId}/projects/{projectId}/dashboard")
public class ExecutiveDashboardController {

    private final ExecutiveDashboardService dashboardService;

    public ExecutiveDashboardController(ExecutiveDashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    @GetMapping
    public ResponseEntity<ExecutiveDashboardResponse> getDashboard(
            @PathVariable String userId, @PathVariable String projectId
    ) {
        return ResponseEntity.ok(dashboardService.getDashboard(userId, projectId));
    }
}
