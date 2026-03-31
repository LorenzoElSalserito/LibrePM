package com.lorenzodm.librepm.api.controller;

import com.lorenzodm.librepm.api.dto.request.ResolveConflictRequest;
import com.lorenzodm.librepm.api.dto.response.AuditEventResponse;
import com.lorenzodm.librepm.api.dto.response.ConflictRecordResponse;
import com.lorenzodm.librepm.api.dto.response.MergePolicyResponse;
import com.lorenzodm.librepm.api.mapper.AuditEventMapper;
import com.lorenzodm.librepm.api.mapper.ConflictRecordMapper;
import com.lorenzodm.librepm.api.mapper.MergePolicyMapper;
import com.lorenzodm.librepm.service.AuditService;
import com.lorenzodm.librepm.service.SyncService;
import com.lorenzodm.librepm.service.impl.ConflictResolutionServiceImpl;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.List;
import java.util.Map;

/**
 * PRD-13: Conflict review, merge policy inspection, audit log endpoints.
 * PRD-13-FR-005: manual conflict review.
 * PRD-13-FR-006: audit log access.
 */
@RestController
@RequestMapping("/api/sync")
public class SyncController {

    private final SyncService syncService;
    private final ConflictResolutionServiceImpl resolutionService;
    private final AuditService auditService;
    private final ConflictRecordMapper conflictMapper;
    private final MergePolicyMapper mergePolicyMapper;
    private final AuditEventMapper auditMapper;

    public SyncController(SyncService syncService,
                          ConflictResolutionServiceImpl resolutionService,
                          AuditService auditService,
                          ConflictRecordMapper conflictMapper,
                          MergePolicyMapper mergePolicyMapper,
                          AuditEventMapper auditMapper) {
        this.syncService = syncService;
        this.resolutionService = resolutionService;
        this.auditService = auditService;
        this.conflictMapper = conflictMapper;
        this.mergePolicyMapper = mergePolicyMapper;
        this.auditMapper = auditMapper;
    }

    // --- Conflict Records (PRD-13-FR-005) ---

    @GetMapping("/conflicts")
    public ResponseEntity<List<ConflictRecordResponse>> listPendingConflicts() {
        return ResponseEntity.ok(syncService.getPendingConflicts()
                .stream().map(conflictMapper::toResponse).toList());
    }

    @GetMapping("/conflicts/{conflictId}")
    public ResponseEntity<ConflictRecordResponse> getConflict(@PathVariable String conflictId) {
        return ResponseEntity.ok(conflictMapper.toResponse(resolutionService.getById(conflictId)));
    }

    @GetMapping("/conflicts/entity/{entityType}/{entityId}")
    public ResponseEntity<List<ConflictRecordResponse>> getConflictsByEntity(
            @PathVariable String entityType, @PathVariable String entityId) {
        return ResponseEntity.ok(resolutionService.listByEntity(entityType, entityId)
                .stream().map(conflictMapper::toResponse).toList());
    }

    @PostMapping("/conflicts/{conflictId}/resolve")
    public ResponseEntity<ConflictRecordResponse> resolveConflict(
            @PathVariable String conflictId,
            @RequestParam(defaultValue = "system") String userId,
            @Valid @RequestBody ResolveConflictRequest req
    ) {
        return ResponseEntity.ok(conflictMapper.toResponse(
                resolutionService.resolve(conflictId, req, userId)));
    }

    @GetMapping("/conflicts/count")
    public ResponseEntity<Map<String, Long>> countPendingConflicts() {
        return ResponseEntity.ok(Map.of("pendingConflicts", syncService.countPendingConflicts()));
    }

    // --- Merge Policies (PRD-13-FR-003) ---

    @GetMapping("/policies")
    public ResponseEntity<List<MergePolicyResponse>> listPolicies() {
        return ResponseEntity.ok(syncService.getPendingConflicts().stream()
                .map(c -> mergePolicyMapper.toResponse(syncService.getPolicyFor(c.getEntityType())))
                .distinct().toList());
    }

    @GetMapping("/policies/{entityType}")
    public ResponseEntity<MergePolicyResponse> getPolicyForEntity(@PathVariable String entityType) {
        return ResponseEntity.ok(mergePolicyMapper.toResponse(syncService.getPolicyFor(entityType)));
    }

    // --- Audit Log (PRD-13-FR-006) ---

    @GetMapping("/audit/entity/{entityType}/{entityId}")
    public ResponseEntity<List<AuditEventResponse>> getAuditByEntity(
            @PathVariable String entityType, @PathVariable String entityId) {
        return ResponseEntity.ok(auditService.getByEntity(entityType, entityId)
                .stream().map(auditMapper::toResponse).toList());
    }

    @GetMapping("/audit/user/{userId}")
    public ResponseEntity<List<AuditEventResponse>> getAuditByUser(@PathVariable String userId) {
        return ResponseEntity.ok(auditService.getByUser(userId)
                .stream().map(auditMapper::toResponse).toList());
    }

    @GetMapping("/audit/since")
    public ResponseEntity<List<AuditEventResponse>> getAuditSince(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant since) {
        return ResponseEntity.ok(auditService.getSince(since)
                .stream().map(auditMapper::toResponse).toList());
    }
}
