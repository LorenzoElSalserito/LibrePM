package com.lorenzodm.librepm.service;

import com.lorenzodm.librepm.api.dto.request.CreateTaskStatusRequest;
import com.lorenzodm.librepm.api.dto.request.UpdateTaskStatusRequest;
import com.lorenzodm.librepm.core.entity.TaskStatus;

import java.util.List;

public interface TaskStatusService {
    TaskStatus create(CreateTaskStatusRequest request);
    TaskStatus getById(String id);
    List<TaskStatus> listAll();
    TaskStatus update(String id, UpdateTaskStatusRequest request);
    void delete(String id);
}
