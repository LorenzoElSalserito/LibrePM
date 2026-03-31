package com.lorenzodm.librepm.api.controller;

import com.lorenzodm.librepm.api.dto.request.CreateTaskChecklistItemRequest;
import com.lorenzodm.librepm.api.dto.request.UpdateTaskChecklistItemRequest;
import com.lorenzodm.librepm.api.dto.response.TaskChecklistItemResponse;
import com.lorenzodm.librepm.service.TaskChecklistService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/users/{userId}/projects/{projectId}/tasks/{taskId}/checklist")
public class TaskChecklistController {

    private final TaskChecklistService checklistService;

    public TaskChecklistController(TaskChecklistService checklistService) {
        this.checklistService = checklistService;
    }

    @GetMapping
    public ResponseEntity<List<TaskChecklistItemResponse>> listItems(
            @PathVariable String userId,
            @PathVariable String projectId,
            @PathVariable String taskId) {
        return ResponseEntity.ok(checklistService.listItems(userId, projectId, taskId));
    }

    @PostMapping
    public ResponseEntity<TaskChecklistItemResponse> createItem(
            @PathVariable String userId,
            @PathVariable String projectId,
            @PathVariable String taskId,
            @Valid @RequestBody CreateTaskChecklistItemRequest request) {
        TaskChecklistItemResponse created = checklistService.createItem(userId, projectId, taskId, request);
        return ResponseEntity.created(URI.create("")).body(created); // Location non necessaria per sub-risorsa
    }

    @PutMapping("/{itemId}")
    public ResponseEntity<TaskChecklistItemResponse> updateItem(
            @PathVariable String userId,
            @PathVariable String projectId,
            @PathVariable String taskId,
            @PathVariable String itemId,
            @Valid @RequestBody UpdateTaskChecklistItemRequest request) {
        return ResponseEntity.ok(checklistService.updateItem(userId, projectId, taskId, itemId, request));
    }

    @PutMapping("/reorder")
    public ResponseEntity<Void> reorderItems(
            @PathVariable String userId,
            @PathVariable String projectId,
            @PathVariable String taskId,
            @RequestBody List<String> orderedItemIds) {
        checklistService.reorderItems(userId, projectId, taskId, orderedItemIds);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{itemId}")
    public ResponseEntity<Void> deleteItem(
            @PathVariable String userId,
            @PathVariable String projectId,
            @PathVariable String taskId,
            @PathVariable String itemId) {
        checklistService.deleteItem(userId, projectId, taskId, itemId);
        return ResponseEntity.noContent().build();
    }
}
