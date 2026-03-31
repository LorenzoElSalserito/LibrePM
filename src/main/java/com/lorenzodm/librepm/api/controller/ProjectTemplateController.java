package com.lorenzodm.librepm.api.controller;

import com.lorenzodm.librepm.api.dto.request.CreateProjectTemplateRequest;
import com.lorenzodm.librepm.api.dto.request.InstantiateTemplateRequest;
import com.lorenzodm.librepm.api.dto.response.ProjectResponse;
import com.lorenzodm.librepm.api.dto.response.ProjectTemplateResponse;
import com.lorenzodm.librepm.api.mapper.ProjectMapper;
import com.lorenzodm.librepm.api.mapper.ProjectTemplateMapper;
import com.lorenzodm.librepm.service.ProjectTemplateService;
import com.lorenzodm.librepm.service.TemplateInstantiationService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

/**
 * PRD-16: Template Gallery and instantiation endpoints.
 * PRD-16-FR-001: gallery navigable via GET /api/templates
 * PRD-16-FR-004: instantiate via POST /api/users/{userId}/projects/from-template/{templateId}
 * PRD-16-FR-012: clone via POST /api/templates/{templateId}/clone
 */
@RestController
public class ProjectTemplateController {

    private final ProjectTemplateService templateService;
    private final TemplateInstantiationService instantiationService;
    private final ProjectTemplateMapper templateMapper;
    private final ProjectMapper projectMapper;

    public ProjectTemplateController(ProjectTemplateService templateService,
                                     TemplateInstantiationService instantiationService,
                                     ProjectTemplateMapper templateMapper,
                                     ProjectMapper projectMapper) {
        this.templateService = templateService;
        this.instantiationService = instantiationService;
        this.templateMapper = templateMapper;
        this.projectMapper = projectMapper;
    }

    // --- Template Gallery (PRD-16-FR-001) ---

    /** Lists all templates visible in the gallery (SYSTEM + WORKSPACE + USER). */
    @GetMapping("/api/templates")
    public ResponseEntity<List<ProjectTemplateResponse>> listAll() {
        return ResponseEntity.ok(templateService.listAll()
                .stream().map(templateMapper::toResponse).toList());
    }

    /** Lists only built-in SYSTEM templates (PRD-16-FR-002). */
    @GetMapping("/api/templates/system")
    public ResponseEntity<List<ProjectTemplateResponse>> listSystem() {
        return ResponseEntity.ok(templateService.listSystemTemplates()
                .stream().map(templateMapper::toResponse).toList());
    }

    @GetMapping("/api/templates/{templateId}")
    public ResponseEntity<ProjectTemplateResponse> get(@PathVariable String templateId) {
        return ResponseEntity.ok(templateMapper.toResponse(templateService.getById(templateId)));
    }

    /** Creates a USER-scoped template (PRD-16-FR-006). */
    @PostMapping("/api/templates")
    public ResponseEntity<ProjectTemplateResponse> create(
            @Valid @RequestBody CreateProjectTemplateRequest req
    ) {
        var created = templateService.create(req);
        return ResponseEntity.created(URI.create("/api/templates/" + created.getId()))
                .body(templateMapper.toResponse(created));
    }

    /**
     * Clones a template into a new USER-scoped copy (PRD-16-FR-012).
     * Modifications to the clone do NOT affect the source (PRD-16-AC-006).
     */
    @PostMapping("/api/templates/{templateId}/clone")
    public ResponseEntity<ProjectTemplateResponse> clone(
            @PathVariable String templateId,
            @RequestParam String newName
    ) {
        var cloned = templateService.clone(templateId, newName);
        return ResponseEntity.created(URI.create("/api/templates/" + cloned.getId()))
                .body(templateMapper.toResponse(cloned));
    }

    /** Deletes a USER-scoped template. SYSTEM templates are protected (PRD-16-BR-001). */
    @DeleteMapping("/api/templates/{templateId}")
    public ResponseEntity<Void> delete(@PathVariable String templateId) {
        templateService.delete(templateId);
        return ResponseEntity.noContent().build();
    }

    // --- Instantiation (PRD-16-FR-004) ---

    /**
     * Creates a new independent project from a template blueprint.
     * PRD-16-BR-002: the created project has no link back to the template.
     * PRD-16-AC-002 … AC-005: template-specific preconfiguration applied.
     */
    @PostMapping("/api/users/{userId}/projects/from-template/{templateId}")
    public ResponseEntity<ProjectResponse> instantiate(
            @PathVariable String userId,
            @PathVariable String templateId,
            @Valid @RequestBody InstantiateTemplateRequest req
    ) {
        var project = instantiationService.instantiate(userId, templateId, req);
        return ResponseEntity.created(
                        URI.create("/api/users/" + userId + "/projects/" + project.getId()))
                .body(projectMapper.toResponse(project));
    }
}
