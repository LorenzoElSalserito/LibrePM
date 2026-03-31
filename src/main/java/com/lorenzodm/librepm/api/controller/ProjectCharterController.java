package com.lorenzodm.librepm.api.controller;

import com.lorenzodm.librepm.api.dto.request.CreateUpdateProjectCharterRequest;
import com.lorenzodm.librepm.api.dto.response.ProjectCharterResponse;
import com.lorenzodm.librepm.api.mapper.ProjectCharterMapper;
import com.lorenzodm.librepm.service.ProjectCharterService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users/{userId}/projects/{projectId}/charter")
public class ProjectCharterController {

    private final ProjectCharterService charterService;
    private final ProjectCharterMapper charterMapper;

    public ProjectCharterController(ProjectCharterService charterService, ProjectCharterMapper charterMapper) {
        this.charterService = charterService;
        this.charterMapper = charterMapper;
    }

    @GetMapping
    public ResponseEntity<ProjectCharterResponse> get(@PathVariable String userId, @PathVariable String projectId) {
        return ResponseEntity.ok(charterMapper.toResponse(charterService.getByProjectId(userId, projectId)));
    }

    @PutMapping
    public ResponseEntity<ProjectCharterResponse> upsert(
            @PathVariable String userId,
            @PathVariable String projectId,
            @Valid @RequestBody CreateUpdateProjectCharterRequest req
    ) {
        return ResponseEntity.ok(charterMapper.toResponse(charterService.upsert(userId, projectId, req)));
    }
}
