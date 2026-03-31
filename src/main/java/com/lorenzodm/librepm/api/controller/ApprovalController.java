package com.lorenzodm.librepm.api.controller;

import com.lorenzodm.librepm.api.dto.response.ApprovalRequestResponse;
import com.lorenzodm.librepm.core.entity.ApprovalRequest;
import com.lorenzodm.librepm.service.ApprovalService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/users/{userId}/approvals")
public class ApprovalController {

    private final ApprovalService service;

    public ApprovalController(ApprovalService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<ApprovalRequestResponse> create(
            @PathVariable String userId,
            @RequestBody Map<String, String> body) {
        ApprovalRequest ar = service.create(
                userId,
                body.get("approverId"),
                body.get("entityType"),
                body.get("entityId"),
                body.get("projectId")
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(toResponse(ar));
    }

    @GetMapping("/pending")
    public ResponseEntity<List<ApprovalRequestResponse>> listPending(@PathVariable String userId) {
        List<ApprovalRequestResponse> response = service.listPending(userId).stream()
                .map(this::toResponse)
                .toList();
        return ResponseEntity.ok(response);
    }

    @GetMapping("/pending/count")
    public ResponseEntity<Map<String, Long>> countPending(@PathVariable String userId) {
        return ResponseEntity.ok(Map.of("count", service.countPending(userId)));
    }

    @PatchMapping("/{approvalId}")
    public ResponseEntity<ApprovalRequestResponse> resolve(
            @PathVariable String userId,
            @PathVariable String approvalId,
            @RequestBody Map<String, String> body) {
        ApprovalRequest ar = service.resolve(userId, approvalId, body.get("status"), body.get("comment"));
        return ResponseEntity.ok(toResponse(ar));
    }

    private ApprovalRequestResponse toResponse(ApprovalRequest ar) {
        return new ApprovalRequestResponse(
                ar.getId(),
                ar.getEntityType(),
                ar.getEntityId(),
                ar.getProject() != null ? ar.getProject().getId() : null,
                ar.getRequestedBy().getId(),
                ar.getRequestedBy().getDisplayName() != null ? ar.getRequestedBy().getDisplayName() : ar.getRequestedBy().getUsername(),
                ar.getApprover().getId(),
                ar.getApprover().getDisplayName() != null ? ar.getApprover().getDisplayName() : ar.getApprover().getUsername(),
                ar.getStatus().name(),
                ar.getComment(),
                ar.getRequestedAt(),
                ar.getResolvedAt()
        );
    }
}
