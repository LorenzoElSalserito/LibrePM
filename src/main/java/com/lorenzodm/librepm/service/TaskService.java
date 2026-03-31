package com.lorenzodm.librepm.service;

import com.lorenzodm.librepm.api.dto.request.CreateTaskRequest;
import com.lorenzodm.librepm.api.dto.request.UpdateTaskRequest;
import com.lorenzodm.librepm.api.dto.request.ChangeTaskStatusRequest;
import com.lorenzodm.librepm.core.entity.Task;

import java.util.List;

public interface TaskService {

    Task create(String userId, String projectId, CreateTaskRequest req);

    Task getOwned(String userId, String projectId, String taskId);

    List<Task> listOwned(
            String userId,
            String projectId,
            String status,
            String priority,
            String search,
            boolean includeArchived
    );

    Task update(String userId, String projectId, String taskId, UpdateTaskRequest req);

    Task updateStatus(String userId, String projectId, String taskId, ChangeTaskStatusRequest req);

    Task setArchived(String userId, String projectId, String taskId, boolean archived);

    void delete(String userId, String projectId, String taskId);

    void reorder(String userId, String projectId, List<String> orderedTaskIds);

    // Dependencies
    void addBlocker(String userId, String projectId, String taskId, String blockerTaskId);
    void removeBlocker(String userId, String projectId, String taskId, String blockerTaskId);

    // Inbox (PRD-01-FR-004)
    Task createInboxTask(String userId, CreateTaskRequest req);
    List<Task> listInboxTasks(String userId);
    Task moveToProject(String userId, String taskId, String projectId);
    long countInboxTasks(String userId);
}
