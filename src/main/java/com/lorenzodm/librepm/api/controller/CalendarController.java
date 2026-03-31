package com.lorenzodm.librepm.api.controller;

import com.lorenzodm.librepm.service.CalendarService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/calendar")
public class CalendarController {

    private final CalendarService calendarService;

    public CalendarController(CalendarService calendarService) {
        this.calendarService = calendarService;
    }

    @GetMapping(value = "/{userId}/feed.ics", produces = "text/calendar")
    public ResponseEntity<String> getFeed(@PathVariable String userId, @RequestParam String token) {
        String ics = calendarService.generateIcsFeed(userId, token);
        
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"librepm.ics\"")
                .contentType(MediaType.parseMediaType("text/calendar"))
                .body(ics);
    }

    @PostMapping("/{userId}/rotate-token")
    public ResponseEntity<Map<String, String>> rotateToken(@PathVariable String userId) {
        String newToken = calendarService.rotateToken(userId);
        return ResponseEntity.ok(Map.of("token", newToken));
    }
}
