package com.lorenzodm.librepm.service.impl;

import com.lorenzodm.librepm.api.dto.request.CreateAssignmentRequest;
import com.lorenzodm.librepm.api.exception.ConflictException;
import com.lorenzodm.librepm.api.exception.ResourceNotFoundException;
import com.lorenzodm.librepm.core.entity.Assignment;
import com.lorenzodm.librepm.core.entity.Notification;
import com.lorenzodm.librepm.core.entity.Role;
import com.lorenzodm.librepm.core.entity.Task;
import com.lorenzodm.librepm.core.entity.User;
import com.lorenzodm.librepm.repository.AssignmentRepository;
import com.lorenzodm.librepm.repository.RoleRepository;
import com.lorenzodm.librepm.repository.TaskRepository;
import com.lorenzodm.librepm.repository.UserRepository;
import com.lorenzodm.librepm.service.AssignmentService;
import com.lorenzodm.librepm.service.NotificationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional
public class AssignmentServiceImpl implements AssignmentService {

    private static final Logger log = LoggerFactory.getLogger(AssignmentServiceImpl.class);
    private final AssignmentRepository assignmentRepository;
    private final TaskRepository taskRepository;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final NotificationService notificationService;

    public AssignmentServiceImpl(
            AssignmentRepository assignmentRepository,
            TaskRepository taskRepository,
            UserRepository userRepository,
            RoleRepository roleRepository,
            NotificationService notificationService) {
        this.assignmentRepository = assignmentRepository;
        this.taskRepository = taskRepository;
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.notificationService = notificationService;
    }

    @Override
    public Assignment create(String currentUserId, CreateAssignmentRequest request) {
        log.debug("Creazione assegnazione: task={}, user={}", request.taskId(), request.userId());

        if (assignmentRepository.existsByTaskIdAndUserId(request.taskId(), request.userId())) {
            throw new ConflictException("Utente già assegnato a questo task");
        }

        Task task = taskRepository.findById(request.taskId())
                .orElseThrow(() -> new ResourceNotFoundException("Task non trovato: " + request.taskId()));
        User user = userRepository.findById(request.userId())
                .orElseThrow(() -> new ResourceNotFoundException("Utente non trovato: " + request.userId()));

        Assignment assignment = new Assignment();
        assignment.setTask(task);
        assignment.setUser(user);
        assignment.setAssignedAt(LocalDateTime.now());

        if (request.roleId() != null && !request.roleId().isBlank()) {
            Role role = roleRepository.findById(request.roleId())
                    .orElseThrow(() -> new ResourceNotFoundException("Ruolo non trovato: " + request.roleId()));
            assignment.setRole(role);
        }

        Assignment saved = assignmentRepository.save(assignment);

        // Send notification to assigned user
        if (!request.userId().equals(currentUserId)) {
            User sender = userRepository.findById(currentUserId).orElse(null);
            notificationService.create(
                    user, sender,
                    Notification.NotificationType.TASK_ASSIGNED,
                    "Sei stato assegnato al task: " + task.getTitle(),
                    "TASK", task.getId());
        }

        return saved;
    }

    @Override
    @Transactional(readOnly = true)
    public Assignment getById(String id) {
        return assignmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Assegnazione non trovata: " + id));
    }

    @Override
    @Transactional(readOnly = true)
    public List<Assignment> findByTaskId(String taskId) {
        return assignmentRepository.findByTaskId(taskId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Assignment> findByUserId(String userId) {
        return assignmentRepository.findByUserId(userId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Assignment> findByProjectId(String projectId) {
        return assignmentRepository.findByProjectId(projectId);
    }

    @Override
    public void delete(String id) {
        log.debug("Eliminazione assegnazione: {}", id);
        Assignment assignment = getById(id);
        assignmentRepository.delete(assignment);
    }
}
