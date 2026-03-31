package com.lorenzodm.librepm.api.controller;

import com.lorenzodm.librepm.api.dto.request.UpdateCalendarFeedRequest;
import com.lorenzodm.librepm.api.dto.response.CalendarFeedTokenResponse;
import com.lorenzodm.librepm.api.mapper.CalendarFeedTokenMapper;
import com.lorenzodm.librepm.service.IcsExportService;
import jakarta.validation.Valid;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * ICS calendar feed endpoints (PRD-14-FR-001, FR-002, BR-001).
 * PRD-14-AC-001: feed MUST be valid RFC 5545.
 */
@RestController
public class CalendarFeedController {

    private final IcsExportService icsExportService;
    private final CalendarFeedTokenMapper mapper;

    public CalendarFeedController(IcsExportService icsExportService,
                                   CalendarFeedTokenMapper mapper) {
        this.icsExportService = icsExportService;
        this.mapper = mapper;
    }

    // --- Token management ---

    /**
     * Returns (or creates) the ICS feed token for a user.
     * PRD-14-FR-001: authenticated users can access their calendar feed.
     */
    @GetMapping("/api/users/{userId}/calendar/token")
    public ResponseEntity<CalendarFeedTokenResponse> getOrCreateToken(@PathVariable String userId) {
        return ResponseEntity.ok(mapper.toResponse(icsExportService.getOrCreateToken(userId)));
    }

    /**
     * Regenerates the feed token (old token immediately invalidated).
     * PRD-14-BR-001: token MUST be regenerable.
     */
    @PostMapping("/api/users/{userId}/calendar/token/regenerate")
    public ResponseEntity<CalendarFeedTokenResponse> regenerateToken(@PathVariable String userId) {
        return ResponseEntity.ok(mapper.toResponse(icsExportService.regenerateToken(userId)));
    }

    /**
     * Updates feed configuration (entity types to include, description).
     * PRD-14-FR-002: configurable entity inclusion.
     */
    @PutMapping("/api/users/{userId}/calendar/token")
    public ResponseEntity<CalendarFeedTokenResponse> updateFeedConfig(
            @PathVariable String userId,
            @Valid @RequestBody UpdateCalendarFeedRequest req) {
        return ResponseEntity.ok(mapper.toResponse(icsExportService.updateFeedConfig(userId, req)));
    }

    // --- Public ICS feed endpoint ---

    /**
     * Public ICS feed URL (no auth — secured by opaque token).
     * PRD-14-AC-001: MUST return valid RFC 5545 iCalendar format.
     * PRD-14-FR-001: subscribable from calendar apps.
     */
    @GetMapping(value = "/api/calendar/feed/{token}.ics",
            produces = "text/calendar;charset=UTF-8")
    public ResponseEntity<String> getIcsFeed(@PathVariable String token) {
        String content = icsExportService.generateIcsContent(token);
        if (content == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType("text/calendar;charset=UTF-8"))
                .header("Content-Disposition", "attachment; filename=\"librepm.ics\"")
                .body(content);
    }
}
