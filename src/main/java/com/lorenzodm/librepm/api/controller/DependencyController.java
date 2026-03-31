package com.lorenzodm.librepm.api.controller;

import com.lorenzodm.librepm.api.dto.request.CreateDependencyRequest;
import com.lorenzodm.librepm.api.dto.response.DependencyResponse;
import com.lorenzodm.librepm.api.mapper.DependencyMapper;
import com.lorenzodm.librepm.service.DependencyService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users/{userId}/projects/{projectId}/dependencies")
public class DependencyController {

    private final DependencyService dependencyService;
    private final DependencyMapper dependencyMapper;

    public DependencyController(DependencyService dependencyService, DependencyMapper dependencyMapper) {
        this.dependencyService = dependencyService;
        this.dependencyMapper = dependencyMapper;
    }

    @PostMapping
    public ResponseEntity<DependencyResponse> create(
            @PathVariable String userId,
            @PathVariable String projectId,
            @Valid @RequestBody CreateDependencyRequest req
    ) {
        return ResponseEntity.ok(dependencyMapper.toResponse(
                dependencyService.create(userId, projectId, req)
        ));
    }

    @GetMapping
    public ResponseEntity<List<DependencyResponse>> listByProject(
            @PathVariable String userId,
            @PathVariable String projectId
    ) {
        return ResponseEntity.ok(
                dependencyService.listByProject(userId, projectId).stream()
                        .map(dependencyMapper::toResponse).toList()
        );
    }

    @GetMapping("/task/{taskId}")
    public ResponseEntity<List<DependencyResponse>> listByTask(
            @PathVariable String userId,
            @PathVariable String projectId,
            @PathVariable String taskId
    ) {
        return ResponseEntity.ok(
                dependencyService.listByTask(userId, projectId, taskId).stream()
                        .map(dependencyMapper::toResponse).toList()
        );
    }

    @DeleteMapping("/{dependencyId}")
    public ResponseEntity<Void> delete(
            @PathVariable String userId,
            @PathVariable String projectId,
            @PathVariable String dependencyId
    ) {
        dependencyService.delete(userId, projectId, dependencyId);
        return ResponseEntity.noContent().build();
    }
}
