package com.lorenzodm.librepm.service;

import com.lorenzodm.librepm.api.dto.request.CreateTaskPriorityRequest;
import com.lorenzodm.librepm.api.dto.request.UpdateTaskPriorityRequest;
import com.lorenzodm.librepm.core.entity.TaskPriority;

import java.util.List;

public interface TaskPriorityService {
    TaskPriority create(CreateTaskPriorityRequest request);
    TaskPriority getById(String id);
    List<TaskPriority> listAll();
    TaskPriority update(String id, UpdateTaskPriorityRequest request);
    void delete(String id);
}
