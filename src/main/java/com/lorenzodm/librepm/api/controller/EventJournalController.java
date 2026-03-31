package com.lorenzodm.librepm.api.controller;

import com.lorenzodm.librepm.core.entity.EventJournalEntry;
import com.lorenzodm.librepm.service.EventJournalService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/events")
public class EventJournalController {

    private final EventJournalService eventJournalService;

    public EventJournalController(EventJournalService eventJournalService) {
        this.eventJournalService = eventJournalService;
    }

    @GetMapping("/recent")
    public ResponseEntity<List<Map<String, Object>>> recent(
            @RequestParam(defaultValue = "100") int limit) {
        List<EventJournalEntry> entries = eventJournalService.recent(limit);
        return ResponseEntity.ok(entries.stream().map(this::toMap).toList());
    }

    @GetMapping
    public ResponseEntity<List<Map<String, Object>>> query(
            @RequestParam(required = false) String from,
            @RequestParam(required = false) String to,
            @RequestParam(required = false) List<String> types,
            @RequestParam(defaultValue = "200") int limit) {
        Instant fromInstant = from != null ? Instant.parse(from) : Instant.now().minus(30, ChronoUnit.DAYS);
        Instant toInstant = to != null ? Instant.parse(to) : Instant.now();
        List<EventJournalEntry> entries = eventJournalService.query(fromInstant, toInstant, types, limit);
        return ResponseEntity.ok(entries.stream().map(this::toMap).toList());
    }

    @PostMapping("/cleanup")
    public ResponseEntity<Map<String, Object>> cleanup(
            @RequestParam(defaultValue = "90") int retentionDays) {
        int deleted = eventJournalService.cleanup(retentionDays);
        return ResponseEntity.ok(Map.of("deletedEntries", deleted));
    }

    private Map<String, Object> toMap(EventJournalEntry e) {
        return Map.of(
                "id", e.getId(),
                "eventType", e.getEventType(),
                "entityType", e.getEntityType() != null ? e.getEntityType() : "",
                "entityId", e.getEntityId() != null ? e.getEntityId() : "",
                "payload", e.getPayload() != null ? e.getPayload() : "",
                "userId", e.getUserId() != null ? e.getUserId() : "",
                "timestamp", e.getTimestamp().toString()
        );
    }
}
