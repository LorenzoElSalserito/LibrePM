package com.lorenzodm.librepm.service;

import com.lorenzodm.librepm.core.entity.ApprovalRequest;

import java.util.List;

public interface ApprovalService {
    ApprovalRequest create(String requesterId, String approverId, String entityType, String entityId, String projectId);
    ApprovalRequest resolve(String userId, String approvalId, String status, String comment);
    List<ApprovalRequest> listPending(String userId);
    List<ApprovalRequest> listByProject(String projectId);
    long countPending(String userId);
}
