package com.lorenzodm.librepm.api.controller;

import com.lorenzodm.librepm.core.entity.AuditEvent;
import com.lorenzodm.librepm.service.AuditService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.List;

/**
 * REST controller for querying audit events.
 * <p>
 * Audit events are append-only and immutable. This controller provides
 * read-only access to the audit trail for history, compliance, and debugging.
 * </p>
 *
 * @author Lorenzo DM
 * @since 0.10.0
 */
@RestController
@RequestMapping("/api/audit")
public class AuditController {

    private final AuditService auditService;

    public AuditController(AuditService auditService) {
        this.auditService = auditService;
    }

    /**
     * Returns audit events for a specific entity.
     *
     * @param entityType the type of entity (e.g., "Task", "Project", "Note")
     * @param entityId   the ID of the entity
     * @return list of audit events, most recent first
     */
    @GetMapping("/entity/{entityType}/{entityId}")
    public ResponseEntity<List<AuditEvent>> getByEntity(
            @PathVariable String entityType,
            @PathVariable String entityId) {
        return ResponseEntity.ok(auditService.getByEntity(entityType, entityId));
    }

    /**
     * Returns audit events triggered by a specific user.
     *
     * @param userId the user ID
     * @return list of audit events, most recent first
     */
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<AuditEvent>> getByUser(@PathVariable String userId) {
        return ResponseEntity.ok(auditService.getByUser(userId));
    }

    /**
     * Returns audit events since a given timestamp.
     *
     * @param since ISO-8601 instant (e.g., "2026-03-25T00:00:00Z")
     * @return list of audit events, most recent first
     */
    @GetMapping("/since")
    public ResponseEntity<List<AuditEvent>> getSince(@RequestParam String since) {
        Instant instant = Instant.parse(since);
        return ResponseEntity.ok(auditService.getSince(instant));
    }
}
