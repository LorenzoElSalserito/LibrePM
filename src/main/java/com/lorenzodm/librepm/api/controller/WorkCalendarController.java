package com.lorenzodm.librepm.api.controller;

import com.lorenzodm.librepm.api.dto.request.AddCalendarExceptionRequest;
import com.lorenzodm.librepm.api.dto.request.AddWorkDayRuleRequest;
import com.lorenzodm.librepm.api.dto.request.CreateWorkCalendarRequest;
import com.lorenzodm.librepm.api.dto.request.UpdateWorkCalendarRequest;
import com.lorenzodm.librepm.api.dto.response.CalendarExceptionResponse;
import com.lorenzodm.librepm.api.dto.response.WorkCalendarResponse;
import com.lorenzodm.librepm.api.dto.response.WorkDayRuleResponse;
import com.lorenzodm.librepm.api.mapper.WorkCalendarMapper;
import com.lorenzodm.librepm.service.WorkCalendarService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/calendars")
public class WorkCalendarController {

    private final WorkCalendarService workCalendarService;
    private final WorkCalendarMapper workCalendarMapper;

    public WorkCalendarController(WorkCalendarService workCalendarService, WorkCalendarMapper workCalendarMapper) {
        this.workCalendarService = workCalendarService;
        this.workCalendarMapper = workCalendarMapper;
    }

    @PostMapping
    public ResponseEntity<WorkCalendarResponse> create(@Valid @RequestBody CreateWorkCalendarRequest req) {
        var created = workCalendarService.create(req);
        return ResponseEntity.created(URI.create("/api/calendars/" + created.getId()))
                .body(workCalendarMapper.toResponse(created));
    }

    @GetMapping
    public ResponseEntity<List<WorkCalendarResponse>> listAll() {
        return ResponseEntity.ok(
                workCalendarService.listAll().stream().map(workCalendarMapper::toResponse).toList()
        );
    }

    @GetMapping("/{calendarId}")
    public ResponseEntity<WorkCalendarResponse> get(@PathVariable String calendarId) {
        return ResponseEntity.ok(workCalendarMapper.toResponse(workCalendarService.getById(calendarId)));
    }

    @PutMapping("/{calendarId}")
    public ResponseEntity<WorkCalendarResponse> update(
            @PathVariable String calendarId,
            @Valid @RequestBody UpdateWorkCalendarRequest req
    ) {
        return ResponseEntity.ok(workCalendarMapper.toResponse(workCalendarService.update(calendarId, req)));
    }

    @DeleteMapping("/{calendarId}")
    public ResponseEntity<Void> delete(@PathVariable String calendarId) {
        workCalendarService.delete(calendarId);
        return ResponseEntity.noContent().build();
    }

    // --- Work Day Rules ---

    @PostMapping("/{calendarId}/rules")
    public ResponseEntity<WorkDayRuleResponse> addRule(
            @PathVariable String calendarId,
            @Valid @RequestBody AddWorkDayRuleRequest req
    ) {
        return ResponseEntity.ok(workCalendarMapper.toRuleResponse(
                workCalendarService.addWorkDayRule(calendarId, req)
        ));
    }

    @DeleteMapping("/{calendarId}/rules/{ruleId}")
    public ResponseEntity<Void> removeRule(@PathVariable String calendarId, @PathVariable String ruleId) {
        workCalendarService.removeWorkDayRule(calendarId, ruleId);
        return ResponseEntity.noContent().build();
    }

    // --- Calendar Exceptions ---

    @PostMapping("/{calendarId}/exceptions")
    public ResponseEntity<CalendarExceptionResponse> addException(
            @PathVariable String calendarId,
            @Valid @RequestBody AddCalendarExceptionRequest req
    ) {
        return ResponseEntity.ok(workCalendarMapper.toExceptionResponse(
                workCalendarService.addException(calendarId, req)
        ));
    }

    @DeleteMapping("/{calendarId}/exceptions/{exceptionId}")
    public ResponseEntity<Void> removeException(
            @PathVariable String calendarId,
            @PathVariable String exceptionId
    ) {
        workCalendarService.removeException(calendarId, exceptionId);
        return ResponseEntity.noContent().build();
    }
}
