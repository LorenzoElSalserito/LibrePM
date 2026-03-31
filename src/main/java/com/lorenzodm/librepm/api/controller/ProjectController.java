package com.lorenzodm.librepm.api.controller;

import com.lorenzodm.librepm.api.dto.request.CreateProjectRequest;
import com.lorenzodm.librepm.api.dto.request.UpdateProjectRequest;
import com.lorenzodm.librepm.api.dto.response.ProjectResponse;
import com.lorenzodm.librepm.api.mapper.ProjectMapper;
import com.lorenzodm.librepm.core.entity.Project;
import com.lorenzodm.librepm.service.ProjectService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/users/{userId}/projects")
public class ProjectController {

    private final ProjectService projectService;
    private final ProjectMapper projectMapper;

    public ProjectController(ProjectService projectService, ProjectMapper projectMapper) {
        this.projectService = projectService;
        this.projectMapper = projectMapper;
    }

    @PostMapping
    public ResponseEntity<ProjectResponse> create(@PathVariable String userId, @Valid @RequestBody CreateProjectRequest req) {
        Project created = projectService.create(userId, req);
        return ResponseEntity.created(URI.create("/api/users/" + userId + "/projects/" + created.getId()))
                .body(projectMapper.toResponse(created));
    }

    @GetMapping("/{projectId}")
    public ResponseEntity<ProjectResponse> get(@PathVariable String userId, @PathVariable String projectId) {
        return ResponseEntity.ok(projectMapper.toResponse(projectService.getOwned(userId, projectId)));
    }

    @GetMapping
    public ResponseEntity<List<ProjectResponse>> list(
            @PathVariable String userId,
            @RequestParam(required = false) Boolean archived,
            @RequestParam(required = false) Boolean favorite,
            @RequestParam(required = false) String search
    ) {
        List<ProjectResponse> out = projectService.listOwned(userId, archived, favorite, search).stream()
                .map(projectMapper::toResponse)
                .toList();
        return ResponseEntity.ok(out);
    }

    @PutMapping("/{projectId}")
    public ResponseEntity<ProjectResponse> update(
            @PathVariable String userId,
            @PathVariable String projectId,
            @Valid @RequestBody UpdateProjectRequest req
    ) {
        return ResponseEntity.ok(projectMapper.toResponse(projectService.update(userId, projectId, req)));
    }

    @PatchMapping("/{projectId}/archived")
    public ResponseEntity<ProjectResponse> setArchived(
            @PathVariable String userId,
            @PathVariable String projectId,
            @RequestParam boolean archived
    ) {
        return ResponseEntity.ok(projectMapper.toResponse(projectService.setArchived(userId, projectId, archived)));
    }
}
