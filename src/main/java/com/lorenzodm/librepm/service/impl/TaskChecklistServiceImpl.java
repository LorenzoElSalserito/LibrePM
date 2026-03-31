package com.lorenzodm.librepm.service.impl;

import com.lorenzodm.librepm.api.dto.request.CreateTaskChecklistItemRequest;
import com.lorenzodm.librepm.api.dto.request.UpdateTaskChecklistItemRequest;
import com.lorenzodm.librepm.api.dto.response.TaskChecklistItemResponse;
import com.lorenzodm.librepm.api.exception.OwnershipViolationException;
import com.lorenzodm.librepm.api.exception.ResourceNotFoundException;
import com.lorenzodm.librepm.core.entity.Task;
import com.lorenzodm.librepm.core.entity.TaskChecklistItem;
import com.lorenzodm.librepm.repository.TaskChecklistItemRepository;
import com.lorenzodm.librepm.repository.TaskRepository;
import com.lorenzodm.librepm.service.TaskChecklistService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class TaskChecklistServiceImpl implements TaskChecklistService {

    private final TaskChecklistItemRepository checklistRepository;
    private final TaskRepository taskRepository;

    public TaskChecklistServiceImpl(TaskChecklistItemRepository checklistRepository, TaskRepository taskRepository) {
        this.checklistRepository = checklistRepository;
        this.taskRepository = taskRepository;
    }

    @Override
    public List<TaskChecklistItemResponse> listItems(String userId, String projectId, String taskId) {
        Task task = getTaskOwned(userId, projectId, taskId);
        return checklistRepository.findByTaskIdOrderBySortOrderAsc(task.getId()).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    public TaskChecklistItemResponse createItem(String userId, String projectId, String taskId, CreateTaskChecklistItemRequest request) {
        Task task = getTaskOwned(userId, projectId, taskId);

        TaskChecklistItem item = new TaskChecklistItem();
        item.setText(request.text());
        item.setSortOrder(request.sortOrder() != null ? request.sortOrder() : 0);
        item.setTask(task);

        return toResponse(checklistRepository.save(item));
    }

    @Override
    public TaskChecklistItemResponse updateItem(String userId, String projectId, String taskId, String itemId, UpdateTaskChecklistItemRequest request) {
        // Verifica ownership task
        getTaskOwned(userId, projectId, taskId);

        TaskChecklistItem item = checklistRepository.findById(itemId)
                .orElseThrow(() -> new ResourceNotFoundException("Checklist item non trovato: " + itemId));

        if (!item.getTask().getId().equals(taskId)) {
            throw new ResourceNotFoundException("Item non appartiene al task specificato");
        }

        if (request.text() != null) item.setText(request.text());
        if (request.done() != null) item.setDone(request.done());
        if (request.sortOrder() != null) item.setSortOrder(request.sortOrder());

        return toResponse(checklistRepository.save(item));
    }

    @Override
    public void deleteItem(String userId, String projectId, String taskId, String itemId) {
        // Verifica ownership task
        getTaskOwned(userId, projectId, taskId);

        TaskChecklistItem item = checklistRepository.findById(itemId)
                .orElseThrow(() -> new ResourceNotFoundException("Checklist item non trovato: " + itemId));

        if (!item.getTask().getId().equals(taskId)) {
            throw new ResourceNotFoundException("Item non appartiene al task specificato");
        }

        checklistRepository.delete(item);
    }

    @Override
    public void reorderItems(String userId, String projectId, String taskId, List<String> orderedItemIds) {
        // Verifica ownership task
        getTaskOwned(userId, projectId, taskId);

        for (int i = 0; i < orderedItemIds.size(); i++) {
            String itemId = orderedItemIds.get(i);
            TaskChecklistItem item = checklistRepository.findById(itemId)
                    .orElseThrow(() -> new ResourceNotFoundException("Checklist item non trovato: " + itemId));
            
            if (!item.getTask().getId().equals(taskId)) {
                throw new ResourceNotFoundException("Item non appartiene al task specificato: " + itemId);
            }

            item.setSortOrder(i);
            checklistRepository.save(item);
        }
    }

    private Task getTaskOwned(String userId, String projectId, String taskId) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new ResourceNotFoundException("Task non trovato: " + taskId));

        if (!task.getProject().getId().equals(projectId)) {
            throw new ResourceNotFoundException("Task non appartiene al progetto specificato");
        }

        if (!task.getProject().getOwner().getId().equals(userId)) {
            throw new OwnershipViolationException("Non sei owner di questo progetto");
        }

        return task;
    }

    private TaskChecklistItemResponse toResponse(TaskChecklistItem item) {
        return new TaskChecklistItemResponse(
                item.getId(),
                item.getText(),
                item.isDone(),
                item.getSortOrder(),
                item.getCreatedAt(),
                item.getUpdatedAt()
        );
    }
}
