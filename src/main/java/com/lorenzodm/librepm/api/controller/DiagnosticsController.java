package com.lorenzodm.librepm.api.controller;

import com.lorenzodm.librepm.service.DiagnosticsService;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

@RestController
@RequestMapping("/api/diagnostics")
public class DiagnosticsController {

    private final DiagnosticsService diagnosticsService;

    public DiagnosticsController(DiagnosticsService diagnosticsService) {
        this.diagnosticsService = diagnosticsService;
    }

    @PostMapping("/generate")
    public ResponseEntity<InputStreamResource> generate() throws IOException {
        Path bundle = diagnosticsService.generateBundle();
        InputStreamResource resource = new InputStreamResource(Files.newInputStream(bundle));

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType("application/zip"))
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"" + bundle.getFileName().toString() + "\"")
                .body(resource);
    }
}
