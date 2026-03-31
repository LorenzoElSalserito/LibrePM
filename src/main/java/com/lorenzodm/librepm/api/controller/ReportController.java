package com.lorenzodm.librepm.api.controller;

import com.lorenzodm.librepm.service.ReportGenerationService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/projects/{projectId}/report")
public class ReportController {

    private final ReportGenerationService reportService;

    public ReportController(ReportGenerationService reportService) {
        this.reportService = reportService;
    }

    @GetMapping
    public ResponseEntity<byte[]> generateReport(
            @PathVariable String projectId,
            @RequestParam(defaultValue = "EXECUTIVE_SUMMARY") String type) {
        ReportGenerationService.ReportType reportType =
                ReportGenerationService.ReportType.valueOf(type.toUpperCase());

        byte[] pdf = reportService.generateReport(projectId, reportType);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"" + type.toLowerCase() + "-" + projectId + ".pdf\"")
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdf);
    }
}
