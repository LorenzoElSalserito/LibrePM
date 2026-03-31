package com.lorenzodm.librepm.service.impl;

import com.lorenzodm.librepm.api.dto.request.CreateTaskStatusRequest;
import com.lorenzodm.librepm.api.dto.request.UpdateTaskStatusRequest;
import com.lorenzodm.librepm.api.exception.ConflictException;
import com.lorenzodm.librepm.api.exception.ResourceNotFoundException;
import com.lorenzodm.librepm.core.entity.TaskStatus;
import com.lorenzodm.librepm.repository.TaskStatusRepository;
import com.lorenzodm.librepm.service.TaskStatusService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class TaskStatusServiceImpl implements TaskStatusService {

    private static final Logger log = LoggerFactory.getLogger(TaskStatusServiceImpl.class);
    private final TaskStatusRepository repository;

    public TaskStatusServiceImpl(TaskStatusRepository repository) {
        this.repository = repository;
    }

    @Override
    public TaskStatus create(CreateTaskStatusRequest request) {
        log.debug("Creazione stato task: {}", request.name());
        if (repository.existsByNameIgnoreCase(request.name())) {
            throw new ConflictException("Stato già esistente: " + request.name());
        }
        TaskStatus status = new TaskStatus();
        status.setName(request.name().trim());
        status.setDescription(request.description());
        status.setColor(request.color());
        return repository.save(status);
    }

    @Override
    @Transactional(readOnly = true)
    public TaskStatus getById(String id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Stato task non trovato: " + id));
    }

    @Override
    @Transactional(readOnly = true)
    public List<TaskStatus> listAll() {
        return repository.findAllActive();
    }

    @Override
    public TaskStatus update(String id, UpdateTaskStatusRequest request) {
        log.debug("Aggiornamento stato task: {}", id);
        TaskStatus status = getById(id);

        if (request.name() != null && !request.name().isBlank()) {
            String newName = request.name().trim();
            if (!status.getName().equals(newName) && repository.existsByNameIgnoreCase(newName)) {
                throw new ConflictException("Stato già esistente: " + newName);
            }
            status.setName(newName);
        }
        if (request.description() != null) {
            status.setDescription(request.description());
        }
        if (request.color() != null) {
            status.setColor(request.color());
        }
        return repository.save(status);
    }

    @Override
    public void delete(String id) {
        log.debug("Eliminazione stato task: {}", id);
        TaskStatus status = getById(id);
        repository.delete(status);
    }
}
