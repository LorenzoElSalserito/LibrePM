package com.lorenzodm.librepm.api.controller;

import com.lorenzodm.librepm.api.dto.request.CreateTaskStatusRequest;
import com.lorenzodm.librepm.api.dto.request.UpdateTaskStatusRequest;
import com.lorenzodm.librepm.api.dto.response.TaskStatusResponse;
import com.lorenzodm.librepm.api.mapper.TaskStatusMapper;
import com.lorenzodm.librepm.core.entity.TaskStatus;
import com.lorenzodm.librepm.service.TaskStatusService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/task-statuses")
public class TaskStatusController {

    private final TaskStatusService service;
    private final TaskStatusMapper mapper;

    public TaskStatusController(TaskStatusService service, TaskStatusMapper mapper) {
        this.service = service;
        this.mapper = mapper;
    }

    @PostMapping
    public ResponseEntity<TaskStatusResponse> create(@Valid @RequestBody CreateTaskStatusRequest request) {
        TaskStatus status = service.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(mapper.toResponse(status));
    }

    @GetMapping
    public ResponseEntity<List<TaskStatusResponse>> listAll() {
        List<TaskStatusResponse> response = service.listAll().stream()
                .map(mapper::toResponse).toList();
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<TaskStatusResponse> getById(@PathVariable String id) {
        return ResponseEntity.ok(mapper.toResponse(service.getById(id)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<TaskStatusResponse> update(
            @PathVariable String id,
            @Valid @RequestBody UpdateTaskStatusRequest request) {
        return ResponseEntity.ok(mapper.toResponse(service.update(id, request)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable String id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}
