package com.lorenzodm.librepm.api.controller;

import com.lorenzodm.librepm.api.dto.request.CreateTaskRequest;
import com.lorenzodm.librepm.api.dto.request.UpdateTaskRequest;
import com.lorenzodm.librepm.api.dto.request.ChangeTaskStatusRequest;
import com.lorenzodm.librepm.api.dto.response.GanttTaskResponse;
import com.lorenzodm.librepm.api.dto.response.TaskResponse;
import com.lorenzodm.librepm.api.mapper.TaskMapper;
import com.lorenzodm.librepm.core.entity.Task;
import com.lorenzodm.librepm.core.entity.TaskStatusHistory;
import com.lorenzodm.librepm.repository.TaskStatusHistoryRepository;
import com.lorenzodm.librepm.core.entity.Notification;
import com.lorenzodm.librepm.core.entity.User;
import com.lorenzodm.librepm.repository.UserRepository;
import com.lorenzodm.librepm.service.NotificationService;
import com.lorenzodm.librepm.service.PlanningEngine;
import com.lorenzodm.librepm.service.TaskService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/users/{userId}/projects/{projectId}/tasks")
public class TaskController {

    private final TaskService taskService;
    private final TaskMapper taskMapper;
    private final PlanningEngine planningEngine;
    private final TaskStatusHistoryRepository taskStatusHistoryRepository;
    private final NotificationService notificationService;
    private final UserRepository userRepository;

    public TaskController(TaskService taskService, TaskMapper taskMapper, PlanningEngine planningEngine,
                          TaskStatusHistoryRepository taskStatusHistoryRepository,
                          NotificationService notificationService, UserRepository userRepository) {
        this.taskService = taskService;
        this.taskMapper = taskMapper;
        this.planningEngine = planningEngine;
        this.taskStatusHistoryRepository = taskStatusHistoryRepository;
        this.notificationService = notificationService;
        this.userRepository = userRepository;
    }

    @PostMapping
    public ResponseEntity<TaskResponse> create(
            @PathVariable String userId,
            @PathVariable String projectId,
            @Valid @RequestBody CreateTaskRequest req
    ) {
        Task created = taskService.create(userId, projectId, req);
        return ResponseEntity.created(URI.create("/api/users/" + userId + "/projects/" + projectId + "/tasks/" + created.getId()))
                .body(taskMapper.toResponse(created));
    }

    @GetMapping("/{taskId}")
    public ResponseEntity<TaskResponse> get(@PathVariable String userId, @PathVariable String projectId, @PathVariable String taskId) {
        return ResponseEntity.ok(taskMapper.toResponse(taskService.getOwned(userId, projectId, taskId)));
    }

    @GetMapping
    public ResponseEntity<List<TaskResponse>> list(
            @PathVariable String userId,
            @PathVariable String projectId,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String priority,
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "false") boolean includeArchived
    ) {
        List<TaskResponse> out = taskService.listOwned(userId, projectId, status, priority, search, includeArchived)
                .stream().map(taskMapper::toResponse).toList();
        return ResponseEntity.ok(out);
    }

    @PutMapping("/{taskId}")
    public ResponseEntity<TaskResponse> update(
            @PathVariable String userId,
            @PathVariable String projectId,
            @PathVariable String taskId,
            @Valid @RequestBody UpdateTaskRequest req
    ) {
        return ResponseEntity.ok(taskMapper.toResponse(taskService.update(userId, projectId, taskId, req)));
    }

    @PatchMapping("/{taskId}/status")
    public ResponseEntity<TaskResponse> updateStatus(
            @PathVariable String userId,
            @PathVariable String projectId,
            @PathVariable String taskId,
            @Valid @RequestBody ChangeTaskStatusRequest req
    ) {
        return ResponseEntity.ok(taskMapper.toResponse(taskService.updateStatus(userId, projectId, taskId, req)));
    }

    @PatchMapping("/{taskId}/archived")
    public ResponseEntity<TaskResponse> setArchived(
            @PathVariable String userId,
            @PathVariable String projectId,
            @PathVariable String taskId,
            @RequestParam boolean archived
    ) {
        return ResponseEntity.ok(taskMapper.toResponse(taskService.setArchived(userId, projectId, taskId, archived)));
    }

    @PutMapping("/reorder")
    public ResponseEntity<Void> reorder(
            @PathVariable String userId,
            @PathVariable String projectId,
            @RequestBody List<String> orderedTaskIds
    ) {
        taskService.reorder(userId, projectId, orderedTaskIds);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{taskId}/blockers")
    public ResponseEntity<Void> addBlocker(
            @PathVariable String userId,
            @PathVariable String projectId,
            @PathVariable String taskId,
            @RequestBody Map<String, String> payload
    ) {
        String blockerTaskId = payload.get("blockerTaskId");
        taskService.addBlocker(userId, projectId, taskId, blockerTaskId);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{taskId}/blockers/{blockerTaskId}")
    public ResponseEntity<Void> removeBlocker(
            @PathVariable String userId,
            @PathVariable String projectId,
            @PathVariable String taskId,
            @PathVariable String blockerTaskId
    ) {
        taskService.removeBlocker(userId, projectId, taskId, blockerTaskId);
        return ResponseEntity.ok().build();
    }

    // --- Gantt / Timeline View (PRD-10) ---

    @GetMapping("/gantt")
    public ResponseEntity<List<GanttTaskResponse>> gantt(
            @PathVariable String userId,
            @PathVariable String projectId
    ) {
        return ResponseEntity.ok(planningEngine.buildGanttData(projectId));
    }

    /**
     * Returns status change history for a task (PRD-01-FR-008).
     */
    @GetMapping("/{taskId}/history")
    public ResponseEntity<List<TaskStatusHistory>> getHistory(
            @PathVariable String userId,
            @PathVariable String projectId,
            @PathVariable String taskId) {
        // Verify access
        taskService.getOwned(userId, projectId, taskId);
        return ResponseEntity.ok(taskStatusHistoryRepository.findByTaskIdOrderByChangedAtDesc(taskId));
    }

    @PostMapping("/{taskId}/request-review")
    public ResponseEntity<Void> requestReview(
            @PathVariable String userId,
            @PathVariable String projectId,
            @PathVariable String taskId,
            @RequestBody Map<String, String> body) {
        String reviewerId = body.get("reviewerId");
        Task task = taskService.getOwned(userId, projectId, taskId);
        User sender = userRepository.findById(userId).orElse(null);
        User reviewer = userRepository.findById(reviewerId).orElse(null);
        if (sender == null || reviewer == null) {
            return ResponseEntity.badRequest().build();
        }

        String message = String.format("Review requested for task \"%s\" by @%s", task.getTitle(), sender.getUsername());
        notificationService.create(reviewer, sender, Notification.NotificationType.REVIEW_REQUESTED, message, "TASK", taskId);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{taskId}")
    public ResponseEntity<Void> delete(
            @PathVariable String userId,
            @PathVariable String projectId,
            @PathVariable String taskId
    ) {
        taskService.delete(userId, projectId, taskId);
        return ResponseEntity.noContent().build();
    }
}
