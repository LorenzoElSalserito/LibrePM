package com.lorenzodm.librepm.api.controller;

import com.lorenzodm.librepm.api.dto.response.ResourceAllocationResponse;
import com.lorenzodm.librepm.service.ResourceService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/projects/{projectId}/resources")
public class ResourceController {

    private final ResourceService resourceService;

    public ResourceController(ResourceService resourceService) {
        this.resourceService = resourceService;
    }

    @GetMapping
    public ResponseEntity<ResourceAllocationResponse> getResourceAllocation(
            @PathVariable String projectId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate
    ) {
        return ResponseEntity.ok(resourceService.getResourceAllocation(projectId, startDate, endDate));
    }
}
