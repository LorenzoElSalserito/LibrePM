package com.lorenzodm.librepm.api.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.lorenzodm.librepm.service.EventJournalService;
import com.lorenzodm.librepm.service.WorkspaceTransferService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.Map;

/**
 * Portable JSON export/import of a user's workspace. Works on every database engine (SQLite and
 * shared MariaDB), unlike the raw .db transfer in {@link DatabaseTransferController}.
 */
@RestController
@RequestMapping("/api/users/{userId}/workspace")
public class WorkspaceTransferController {

    private final WorkspaceTransferService workspaceTransferService;
    private final EventJournalService eventJournalService;

    public WorkspaceTransferController(WorkspaceTransferService workspaceTransferService,
                                       EventJournalService eventJournalService) {
        this.workspaceTransferService = workspaceTransferService;
        this.eventJournalService = eventJournalService;
    }

    @GetMapping(value = "/export", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Map<String, Object>> export(@PathVariable String userId) {
        Map<String, Object> doc = workspaceTransferService.export(userId);
        eventJournalService.record("WORKSPACE_EXPORT", "USER", userId, "{\"format\":\"json\"}", userId);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"LibrePM-export-" + LocalDate.now() + ".json\"")
                .body(doc);
    }

    @PostMapping(value = "/import", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<WorkspaceTransferService.ImportSummary> importWorkspace(
            @PathVariable String userId,
            @RequestBody JsonNode body) {
        WorkspaceTransferService.ImportSummary summary = workspaceTransferService.importWorkspace(userId, body);
        eventJournalService.record("WORKSPACE_IMPORT", "USER", userId,
                "{\"projects\":" + summary.projects() + ",\"tasks\":" + summary.tasks() + "}", userId);
        return ResponseEntity.ok(summary);
    }
}
