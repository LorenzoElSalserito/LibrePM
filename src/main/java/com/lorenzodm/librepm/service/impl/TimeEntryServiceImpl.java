package com.lorenzodm.librepm.service.impl;

import com.lorenzodm.librepm.api.dto.request.CreateTimeEntryRequest;
import com.lorenzodm.librepm.api.dto.request.UpdateTimeEntryRequest;
import com.lorenzodm.librepm.api.dto.response.EffortDeviationResponse;
import com.lorenzodm.librepm.api.exception.ResourceNotFoundException;
import com.lorenzodm.librepm.core.entity.Task;
import com.lorenzodm.librepm.core.entity.TimeEntry;
import com.lorenzodm.librepm.core.entity.User;
import com.lorenzodm.librepm.repository.TaskRepository;
import com.lorenzodm.librepm.repository.TimeEntryRepository;
import com.lorenzodm.librepm.repository.UserRepository;
import com.lorenzodm.librepm.service.TimeEntryService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional
public class TimeEntryServiceImpl implements TimeEntryService {

    private static final Logger log = LoggerFactory.getLogger(TimeEntryServiceImpl.class);
    private final TimeEntryRepository timeEntryRepository;
    private final TaskRepository taskRepository;
    private final UserRepository userRepository;

    public TimeEntryServiceImpl(
            TimeEntryRepository timeEntryRepository,
            TaskRepository taskRepository,
            UserRepository userRepository) {
        this.timeEntryRepository = timeEntryRepository;
        this.taskRepository = taskRepository;
        this.userRepository = userRepository;
    }

    @Override
    public TimeEntry create(String userId, CreateTimeEntryRequest request) {
        log.debug("Creazione time entry: task={}, user={}, minutes={}", 
                request.taskId(), userId, request.durationMinutes());

        Task task = taskRepository.findById(request.taskId())
                .orElseThrow(() -> new ResourceNotFoundException("Task non trovato: " + request.taskId()));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Utente non trovato: " + userId));

        TimeEntry.EntryType entryType;
        try {
            entryType = TimeEntry.EntryType.valueOf(request.type().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Tipo entry non valido: " + request.type());
        }

        TimeEntry entry = new TimeEntry();
        entry.setTask(task);
        entry.setUser(user);
        entry.setEntryDate(request.entryDate());
        entry.setDurationMinutes(request.durationMinutes());
        entry.setType(entryType);
        entry.setDescription(request.description());

        TimeEntry saved = timeEntryRepository.save(entry);

        // Update actual effort on the task
        updateTaskActualEffort(task);

        return saved;
    }

    @Override
    @Transactional(readOnly = true)
    public TimeEntry getById(String id) {
        return timeEntryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Time entry non trovata: " + id));
    }

    @Override
    public TimeEntry update(String id, UpdateTimeEntryRequest request) {
        log.debug("Aggiornamento time entry: {}", id);
        TimeEntry entry = getById(id);

        if (request.entryDate() != null) {
            entry.setEntryDate(request.entryDate());
        }
        if (request.durationMinutes() != null) {
            entry.setDurationMinutes(request.durationMinutes());
        }
        if (request.description() != null) {
            entry.setDescription(request.description());
        }

        TimeEntry saved = timeEntryRepository.save(entry);

        // Recalculate actual effort
        updateTaskActualEffort(entry.getTask());

        return saved;
    }

    @Override
    public void delete(String id) {
        log.debug("Eliminazione time entry: {}", id);
        TimeEntry entry = getById(id);
        Task task = entry.getTask();
        timeEntryRepository.delete(entry);
        updateTaskActualEffort(task);
    }

    @Override
    @Transactional(readOnly = true)
    public List<TimeEntry> findByTaskId(String taskId) {
        return timeEntryRepository.findActiveByTaskId(taskId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<TimeEntry> findByUserId(String userId) {
        return timeEntryRepository.findActiveByUserId(userId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<TimeEntry> findByUserIdAndDateRange(String userId, LocalDateTime start, LocalDateTime end) {
        return timeEntryRepository.findByUserIdAndDateRange(userId, start, end);
    }

    @Override
    @Transactional(readOnly = true)
    public List<TimeEntry> findByProjectId(String projectId) {
        return timeEntryRepository.findByProjectId(projectId);
    }

    @Override
    @Transactional(readOnly = true)
    public int getTotalMinutesByTaskId(String taskId) {
        return timeEntryRepository.sumDurationByTaskId(taskId);
    }

    @Override
    @Transactional(readOnly = true)
    public int getTotalMinutesByProjectId(String projectId) {
        return timeEntryRepository.sumDurationByProjectId(projectId);
    }

    @Override
    @Transactional(readOnly = true)
    public EffortDeviationResponse calculateDeviation(String taskId) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new ResourceNotFoundException("Task non trovato: " + taskId));

        int actualMinutes = timeEntryRepository.sumDurationByTaskId(taskId);
        Integer estimatedMinutes = task.getEstimatedEffort();

        int deviationMinutes = 0;
        double deviationPercentage = 0.0;

        if (estimatedMinutes != null && estimatedMinutes > 0) {
            deviationMinutes = actualMinutes - estimatedMinutes;
            deviationPercentage = ((double) deviationMinutes / estimatedMinutes) * 100.0;
        }

        return new EffortDeviationResponse(
                taskId,
                task.getTitle(),
                estimatedMinutes,
                actualMinutes,
                deviationMinutes,
                Math.round(deviationPercentage * 100.0) / 100.0
        );
    }

    @Override
    @Transactional(readOnly = true)
    public List<EffortDeviationResponse> calculateProjectVariance(String projectId) {
        List<Task> tasks = taskRepository.findByProjectId(projectId);
        return tasks.stream()
                .filter(t -> t.getEstimatedEffort() != null && t.getEstimatedEffort() > 0)
                .map(t -> calculateDeviation(t.getId()))
                .toList();
    }

    private void updateTaskActualEffort(Task task) {
        int totalMinutes = timeEntryRepository.sumDurationByTaskId(task.getId());
        task.setActualEffort(totalMinutes);
        taskRepository.save(task);
    }
}
