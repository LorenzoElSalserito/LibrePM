package com.lorenzodm.librepm.api.controller;

import com.lorenzodm.librepm.api.dto.request.CreateResourceAllocationRequest;
import com.lorenzodm.librepm.api.dto.request.UpdateResourceAllocationRequest;
import com.lorenzodm.librepm.api.dto.response.ResourceAllocationItemResponse;
import com.lorenzodm.librepm.api.dto.response.WorkloadSliceResponse;
import com.lorenzodm.librepm.api.mapper.ResourceAllocationMapper;
import com.lorenzodm.librepm.service.ResourceAllocationService;
import com.lorenzodm.librepm.service.WorkloadService;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.time.LocalDate;
import java.util.List;

@RestController
public class ResourceAllocationController {

    private final ResourceAllocationService resourceAllocationService;
    private final WorkloadService workloadService;
    private final ResourceAllocationMapper resourceAllocationMapper;

    public ResourceAllocationController(ResourceAllocationService resourceAllocationService,
                                        WorkloadService workloadService,
                                        ResourceAllocationMapper resourceAllocationMapper) {
        this.resourceAllocationService = resourceAllocationService;
        this.workloadService = workloadService;
        this.resourceAllocationMapper = resourceAllocationMapper;
    }

    // --- Resource Allocations CRUD ---

    @PostMapping("/api/resource-allocations")
    public ResponseEntity<ResourceAllocationItemResponse> create(
            @Valid @RequestBody CreateResourceAllocationRequest req
    ) {
        var created = resourceAllocationService.create(req);
        return ResponseEntity.created(URI.create("/api/resource-allocations/" + created.getId()))
                .body(resourceAllocationMapper.toResponse(created));
    }

    @GetMapping("/api/resource-allocations/{allocationId}")
    public ResponseEntity<ResourceAllocationItemResponse> get(@PathVariable String allocationId) {
        return ResponseEntity.ok(resourceAllocationMapper.toResponse(
                resourceAllocationService.getById(allocationId)));
    }

    @GetMapping("/api/users/{userId}/resource-allocations")
    public ResponseEntity<List<ResourceAllocationItemResponse>> listByUser(@PathVariable String userId) {
        return ResponseEntity.ok(resourceAllocationService.listByUser(userId)
                .stream().map(resourceAllocationMapper::toResponse).toList());
    }

    @GetMapping("/api/users/{userId}/projects/{projectId}/resource-allocations")
    public ResponseEntity<List<ResourceAllocationItemResponse>> listByProject(
            @PathVariable String userId, @PathVariable String projectId
    ) {
        return ResponseEntity.ok(resourceAllocationService.listByProject(projectId)
                .stream().map(resourceAllocationMapper::toResponse).toList());
    }

    @PutMapping("/api/resource-allocations/{allocationId}")
    public ResponseEntity<ResourceAllocationItemResponse> update(
            @PathVariable String allocationId,
            @Valid @RequestBody UpdateResourceAllocationRequest req
    ) {
        return ResponseEntity.ok(resourceAllocationMapper.toResponse(
                resourceAllocationService.update(allocationId, req)));
    }

    @DeleteMapping("/api/resource-allocations/{allocationId}")
    public ResponseEntity<Void> delete(@PathVariable String allocationId) {
        resourceAllocationService.delete(allocationId);
        return ResponseEntity.noContent().build();
    }

    // --- Workload Views (PRD-12) ---

    @GetMapping("/api/users/{userId}/projects/{projectId}/workload")
    public ResponseEntity<List<WorkloadSliceResponse>> getProjectWorkload(
            @PathVariable String userId, @PathVariable String projectId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to
    ) {
        return ResponseEntity.ok(workloadService.getProjectWorkload(userId, projectId, from, to));
    }

    @GetMapping("/api/users/{userId}/workload")
    public ResponseEntity<WorkloadSliceResponse> getUserWorkload(
            @PathVariable String userId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to
    ) {
        return ResponseEntity.ok(workloadService.getUserWorkload(userId, from, to));
    }
}
