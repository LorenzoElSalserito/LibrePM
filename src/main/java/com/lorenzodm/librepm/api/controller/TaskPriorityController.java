package com.lorenzodm.librepm.api.controller;

import com.lorenzodm.librepm.api.dto.request.CreateTaskPriorityRequest;
import com.lorenzodm.librepm.api.dto.request.UpdateTaskPriorityRequest;
import com.lorenzodm.librepm.api.dto.response.TaskPriorityResponse;
import com.lorenzodm.librepm.api.mapper.TaskPriorityMapper;
import com.lorenzodm.librepm.core.entity.TaskPriority;
import com.lorenzodm.librepm.service.TaskPriorityService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/task-priorities")
public class TaskPriorityController {

    private final TaskPriorityService service;
    private final TaskPriorityMapper mapper;

    public TaskPriorityController(TaskPriorityService service, TaskPriorityMapper mapper) {
        this.service = service;
        this.mapper = mapper;
    }

    @PostMapping
    public ResponseEntity<TaskPriorityResponse> create(@Valid @RequestBody CreateTaskPriorityRequest request) {
        TaskPriority priority = service.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(mapper.toResponse(priority));
    }

    @GetMapping
    public ResponseEntity<List<TaskPriorityResponse>> listAll() {
        List<TaskPriorityResponse> response = service.listAll().stream()
                .map(mapper::toResponse).toList();
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<TaskPriorityResponse> getById(@PathVariable String id) {
        return ResponseEntity.ok(mapper.toResponse(service.getById(id)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<TaskPriorityResponse> update(
            @PathVariable String id,
            @Valid @RequestBody UpdateTaskPriorityRequest request) {
        return ResponseEntity.ok(mapper.toResponse(service.update(id, request)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable String id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}
