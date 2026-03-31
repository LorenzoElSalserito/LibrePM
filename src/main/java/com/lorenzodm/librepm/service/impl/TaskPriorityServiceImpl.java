package com.lorenzodm.librepm.service.impl;

import com.lorenzodm.librepm.api.dto.request.CreateTaskPriorityRequest;
import com.lorenzodm.librepm.api.dto.request.UpdateTaskPriorityRequest;
import com.lorenzodm.librepm.api.exception.ConflictException;
import com.lorenzodm.librepm.api.exception.ResourceNotFoundException;
import com.lorenzodm.librepm.core.entity.TaskPriority;
import com.lorenzodm.librepm.repository.TaskPriorityRepository;
import com.lorenzodm.librepm.service.TaskPriorityService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class TaskPriorityServiceImpl implements TaskPriorityService {

    private static final Logger log = LoggerFactory.getLogger(TaskPriorityServiceImpl.class);
    private final TaskPriorityRepository repository;

    public TaskPriorityServiceImpl(TaskPriorityRepository repository) {
        this.repository = repository;
    }

    @Override
    public TaskPriority create(CreateTaskPriorityRequest request) {
        log.debug("Creazione priorità task: {}", request.name());
        if (repository.existsByNameIgnoreCase(request.name())) {
            throw new ConflictException("Priorità già esistente: " + request.name());
        }
        TaskPriority priority = new TaskPriority();
        priority.setName(request.name().trim());
        priority.setLevel(request.level());
        priority.setColor(request.color());
        return repository.save(priority);
    }

    @Override
    @Transactional(readOnly = true)
    public TaskPriority getById(String id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Priorità task non trovata: " + id));
    }

    @Override
    @Transactional(readOnly = true)
    public List<TaskPriority> listAll() {
        return repository.findAllActiveOrderByLevel();
    }

    @Override
    public TaskPriority update(String id, UpdateTaskPriorityRequest request) {
        log.debug("Aggiornamento priorità task: {}", id);
        TaskPriority priority = getById(id);

        if (request.name() != null && !request.name().isBlank()) {
            String newName = request.name().trim();
            if (!priority.getName().equals(newName) && repository.existsByNameIgnoreCase(newName)) {
                throw new ConflictException("Priorità già esistente: " + newName);
            }
            priority.setName(newName);
        }
        if (request.level() != null) {
            priority.setLevel(request.level());
        }
        if (request.color() != null) {
            priority.setColor(request.color());
        }
        return repository.save(priority);
    }

    @Override
    public void delete(String id) {
        log.debug("Eliminazione priorità task: {}", id);
        TaskPriority priority = getById(id);
        repository.delete(priority);
    }
}
