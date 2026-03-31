package com.lorenzodm.librepm.service.impl;

import com.lorenzodm.librepm.api.exception.ForbiddenException;
import com.lorenzodm.librepm.api.exception.ResourceNotFoundException;
import com.lorenzodm.librepm.core.entity.ApprovalRequest;
import com.lorenzodm.librepm.core.entity.Notification;
import com.lorenzodm.librepm.core.entity.Project;
import com.lorenzodm.librepm.core.entity.User;
import com.lorenzodm.librepm.repository.ApprovalRequestRepository;
import com.lorenzodm.librepm.repository.ProjectRepository;
import com.lorenzodm.librepm.repository.UserRepository;
import com.lorenzodm.librepm.service.ApprovalService;
import com.lorenzodm.librepm.service.NotificationService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

@Service
@Transactional
public class ApprovalServiceImpl implements ApprovalService {

    private final ApprovalRequestRepository repository;
    private final UserRepository userRepository;
    private final ProjectRepository projectRepository;
    private final NotificationService notificationService;

    public ApprovalServiceImpl(ApprovalRequestRepository repository,
                                UserRepository userRepository,
                                ProjectRepository projectRepository,
                                NotificationService notificationService) {
        this.repository = repository;
        this.userRepository = userRepository;
        this.projectRepository = projectRepository;
        this.notificationService = notificationService;
    }

    @Override
    public ApprovalRequest create(String requesterId, String approverId, String entityType, String entityId, String projectId) {
        User requester = userRepository.findById(requesterId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        User approver = userRepository.findById(approverId)
                .orElseThrow(() -> new ResourceNotFoundException("Approver not found"));

        ApprovalRequest ar = new ApprovalRequest();
        ar.setRequestedBy(requester);
        ar.setApprover(approver);
        ar.setEntityType(entityType);
        ar.setEntityId(entityId);

        if (projectId != null) {
            Project project = projectRepository.findById(projectId).orElse(null);
            ar.setProject(project);
        }

        ApprovalRequest saved = repository.save(ar);

        // Notify the approver
        String message = String.format("Approval requested for %s by @%s", entityType.toLowerCase().replace('_', ' '), requester.getUsername());
        notificationService.create(approver, requester, Notification.NotificationType.APPROVAL_REQUESTED, message, entityType, entityId);

        return saved;
    }

    @Override
    public ApprovalRequest resolve(String userId, String approvalId, String status, String comment) {
        ApprovalRequest ar = repository.findById(approvalId)
                .orElseThrow(() -> new ResourceNotFoundException("Approval request not found"));

        if (!ar.getApprover().getId().equals(userId)) {
            throw new ForbiddenException("Only the designated approver can resolve this request");
        }

        ar.setStatus(ApprovalRequest.Status.valueOf(status));
        ar.setComment(comment);
        ar.setResolvedAt(Instant.now());

        ApprovalRequest saved = repository.save(ar);

        // Notify the requester
        String message = String.format("Your %s request was %s", ar.getEntityType().toLowerCase().replace('_', ' '), status.toLowerCase());
        notificationService.create(ar.getRequestedBy(), ar.getApprover(), Notification.NotificationType.APPROVAL_RESOLVED, message, ar.getEntityType(), ar.getEntityId());

        return saved;
    }

    @Override
    @Transactional(readOnly = true)
    public List<ApprovalRequest> listPending(String userId) {
        return repository.findPendingByApproverId(userId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ApprovalRequest> listByProject(String projectId) {
        return repository.findByProjectId(projectId);
    }

    @Override
    @Transactional(readOnly = true)
    public long countPending(String userId) {
        return repository.countPendingByApproverId(userId);
    }
}
