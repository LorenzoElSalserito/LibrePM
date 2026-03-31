package com.lorenzodm.librepm.api.controller;

import com.lorenzodm.librepm.api.dto.request.CreateTaskRequest;
import com.lorenzodm.librepm.api.dto.response.TaskResponse;
import com.lorenzodm.librepm.api.mapper.TaskMapper;
import com.lorenzodm.librepm.core.entity.Task;
import com.lorenzodm.librepm.service.TaskService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;
import java.util.Map;

/**
 * REST controller for personal inbox tasks (PRD-01-FR-004).
 * <p>
 * Inbox tasks are tasks without a real project assignment.
 * They can be created quickly and later moved to a project.
 * </p>
 *
 * @author Lorenzo DM
 * @since 0.10.0
 */
@RestController
@RequestMapping("/api/users/{userId}/inbox")
public class InboxController {

    private final TaskService taskService;
    private final TaskMapper taskMapper;

    public InboxController(TaskService taskService, TaskMapper taskMapper) {
        this.taskService = taskService;
        this.taskMapper = taskMapper;
    }

    @PostMapping
    public ResponseEntity<TaskResponse> create(
            @PathVariable String userId,
            @Valid @RequestBody CreateTaskRequest req
    ) {
        Task created = taskService.createInboxTask(userId, req);
        return ResponseEntity.created(URI.create("/api/users/" + userId + "/inbox/" + created.getId()))
                .body(taskMapper.toResponse(created));
    }

    @GetMapping
    public ResponseEntity<List<TaskResponse>> list(@PathVariable String userId) {
        List<TaskResponse> out = taskService.listInboxTasks(userId)
                .stream().map(taskMapper::toResponse).toList();
        return ResponseEntity.ok(out);
    }

    @GetMapping("/count")
    public ResponseEntity<Map<String, Long>> count(@PathVariable String userId) {
        return ResponseEntity.ok(Map.of("count", taskService.countInboxTasks(userId)));
    }

    @PutMapping("/{taskId}/move-to-project")
    public ResponseEntity<TaskResponse> moveToProject(
            @PathVariable String userId,
            @PathVariable String taskId,
            @RequestBody Map<String, String> payload
    ) {
        String projectId = payload.get("projectId");
        Task moved = taskService.moveToProject(userId, taskId, projectId);
        return ResponseEntity.ok(taskMapper.toResponse(moved));
    }
}
