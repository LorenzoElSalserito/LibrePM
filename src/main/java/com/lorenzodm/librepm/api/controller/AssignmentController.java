package com.lorenzodm.librepm.api.controller;

import com.lorenzodm.librepm.api.dto.request.CreateAssignmentRequest;
import com.lorenzodm.librepm.api.dto.response.AssignmentResponse;
import com.lorenzodm.librepm.api.mapper.AssignmentMapper;
import com.lorenzodm.librepm.core.entity.Assignment;
import com.lorenzodm.librepm.security.CurrentUser;
import com.lorenzodm.librepm.service.AssignmentService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/assignments")
public class AssignmentController {

    private final AssignmentService service;
    private final AssignmentMapper mapper;

    public AssignmentController(AssignmentService service, AssignmentMapper mapper) {
        this.service = service;
        this.mapper = mapper;
    }

    @PostMapping
    public ResponseEntity<AssignmentResponse> create(
            @CurrentUser String userId,
            @Valid @RequestBody CreateAssignmentRequest request) {
        Assignment assignment = service.create(userId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(mapper.toResponse(assignment));
    }

    @GetMapping("/{id}")
    public ResponseEntity<AssignmentResponse> getById(@PathVariable String id) {
        return ResponseEntity.ok(mapper.toResponse(service.getById(id)));
    }

    @GetMapping("/task/{taskId}")
    public ResponseEntity<List<AssignmentResponse>> findByTaskId(@PathVariable String taskId) {
        List<AssignmentResponse> response = service.findByTaskId(taskId).stream()
                .map(mapper::toResponse).toList();
        return ResponseEntity.ok(response);
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<AssignmentResponse>> findByUserId(@PathVariable String userId) {
        List<AssignmentResponse> response = service.findByUserId(userId).stream()
                .map(mapper::toResponse).toList();
        return ResponseEntity.ok(response);
    }

    @GetMapping("/project/{projectId}")
    public ResponseEntity<List<AssignmentResponse>> findByProjectId(@PathVariable String projectId) {
        List<AssignmentResponse> response = service.findByProjectId(projectId).stream()
                .map(mapper::toResponse).toList();
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable String id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}
