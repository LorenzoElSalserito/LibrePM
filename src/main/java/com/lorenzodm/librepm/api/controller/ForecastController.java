package com.lorenzodm.librepm.api.controller;

import com.lorenzodm.librepm.service.ForecastService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.Map;

@RestController
@RequestMapping("/api/projects/{projectId}/forecast")
public class ForecastController {

    private final ForecastService forecastService;

    public ForecastController(ForecastService forecastService) {
        this.forecastService = forecastService;
    }

    @GetMapping("/earned-value")
    public ResponseEntity<Map<String, Object>> getEarnedValueMetrics(
            @PathVariable String projectId,
            @RequestParam(required = false) String asOfDate) {
        LocalDate date = asOfDate != null ? LocalDate.parse(asOfDate) : LocalDate.now();
        return ResponseEntity.ok(forecastService.computeEarnedValueMetrics(projectId, date));
    }

    @GetMapping("/resource-issues")
    public ResponseEntity<Map<String, Object>> getResourceIssues(
            @PathVariable String projectId,
            @RequestParam String from, @RequestParam String to) {
        return ResponseEntity.ok(
                forecastService.detectResourceIssues(projectId, LocalDate.parse(from), LocalDate.parse(to))
        );
    }
}
