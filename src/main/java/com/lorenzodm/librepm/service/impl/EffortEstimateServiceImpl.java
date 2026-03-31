package com.lorenzodm.librepm.service.impl;

import com.lorenzodm.librepm.api.dto.request.CreateEffortEstimateRequest;
import com.lorenzodm.librepm.api.exception.ResourceNotFoundException;
import com.lorenzodm.librepm.core.entity.EffortEstimate;
import com.lorenzodm.librepm.core.entity.Task;
import com.lorenzodm.librepm.core.entity.User;
import com.lorenzodm.librepm.repository.EffortEstimateRepository;
import com.lorenzodm.librepm.repository.TaskRepository;
import com.lorenzodm.librepm.repository.UserRepository;
import com.lorenzodm.librepm.service.EffortEstimateService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional
public class EffortEstimateServiceImpl implements EffortEstimateService {

    private static final Logger log = LoggerFactory.getLogger(EffortEstimateServiceImpl.class);
    private final EffortEstimateRepository estimateRepository;
    private final TaskRepository taskRepository;
    private final UserRepository userRepository;

    public EffortEstimateServiceImpl(
            EffortEstimateRepository estimateRepository,
            TaskRepository taskRepository,
            UserRepository userRepository) {
        this.estimateRepository = estimateRepository;
        this.taskRepository = taskRepository;
        this.userRepository = userRepository;
    }

    @Override
    public EffortEstimate create(String userId, CreateEffortEstimateRequest request) {
        log.debug("Creazione stima effort: task={}, minutes={}", request.taskId(), request.estimatedMinutes());

        Task task = taskRepository.findById(request.taskId())
                .orElseThrow(() -> new ResourceNotFoundException("Task non trovato: " + request.taskId()));

        User estimator = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Utente non trovato: " + userId));

        // Determine revision number and link to previous estimate
        EffortEstimate previous = estimateRepository.findLatestByTaskId(request.taskId()).orElse(null);

        EffortEstimate estimate = new EffortEstimate();
        estimate.setTask(task);
        estimate.setEstimator(estimator);
        estimate.setEstimatedMinutes(request.estimatedMinutes());
        estimate.setEstimationDate(LocalDateTime.now());
        estimate.setRationale(request.rationale());
        estimate.setRevisionNumber(previous != null ? (previous.getRevisionNumber() != null ? previous.getRevisionNumber() + 1 : 2) : 1);
        estimate.setPreviousEstimateId(previous != null ? previous.getId() : null);

        EffortEstimate saved = estimateRepository.save(estimate);

        // Update the task's estimated effort with the latest estimate
        task.setEstimatedEffort(request.estimatedMinutes());
        taskRepository.save(task);

        return saved;
    }

    @Override
    @Transactional(readOnly = true)
    public EffortEstimate getById(String id) {
        return estimateRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Stima effort non trovata: " + id));
    }

    @Override
    @Transactional(readOnly = true)
    public List<EffortEstimate> findByTaskId(String taskId) {
        return estimateRepository.findByTaskIdOrderByDateDesc(taskId);
    }

    @Override
    @Transactional(readOnly = true)
    public EffortEstimate findLatestByTaskId(String taskId) {
        return estimateRepository.findLatestByTaskId(taskId)
                .orElseThrow(() -> new ResourceNotFoundException("Nessuna stima trovata per il task: " + taskId));
    }

    @Override
    @Transactional(readOnly = true)
    public List<EffortEstimate> findByProjectId(String projectId) {
        return estimateRepository.findByProjectId(projectId);
    }

    @Override
    public void delete(String id) {
        log.debug("Eliminazione stima effort: {}", id);
        EffortEstimate estimate = getById(id);
        estimateRepository.delete(estimate);
    }
}
