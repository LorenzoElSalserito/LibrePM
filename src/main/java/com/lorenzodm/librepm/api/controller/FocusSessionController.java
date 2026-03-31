package com.lorenzodm.librepm.api.controller;

import com.lorenzodm.librepm.api.dto.request.StartFocusSessionRequest;
import com.lorenzodm.librepm.api.dto.request.StopFocusSessionRequest;
import com.lorenzodm.librepm.api.dto.response.FocusSessionResponse;
import com.lorenzodm.librepm.api.mapper.FocusSessionMapper;
import com.lorenzodm.librepm.core.entity.FocusSession;
import com.lorenzodm.librepm.service.FocusSessionService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/users/{userId}/focus-sessions")
public class FocusSessionController {

    private final FocusSessionService focusSessionService;
    private final FocusSessionMapper focusSessionMapper;

    public FocusSessionController(FocusSessionService focusSessionService, FocusSessionMapper focusSessionMapper) {
        this.focusSessionService = focusSessionService;
        this.focusSessionMapper = focusSessionMapper;
    }

    @PostMapping("/tasks/{taskId}/start")
    public ResponseEntity<FocusSessionResponse> start(
            @PathVariable String userId,
            @PathVariable String taskId,
            @Valid @RequestBody(required = false) StartFocusSessionRequest req
    ) {
        FocusSession created = focusSessionService.start(userId, taskId, req);
        return ResponseEntity.created(URI.create("/api/users/" + userId + "/focus-sessions/" + created.getId()))
                .body(focusSessionMapper.toResponse(created));
    }

    @PostMapping("/{sessionId}/stop")
    public ResponseEntity<FocusSessionResponse> stop(
            @PathVariable String userId,
            @PathVariable String sessionId,
            @Valid @RequestBody(required = false) StopFocusSessionRequest req
    ) {
        return ResponseEntity.ok(focusSessionMapper.toResponse(focusSessionService.stop(userId, sessionId, req)));
    }

    @PostMapping("/current/stop")
    public ResponseEntity<FocusSessionResponse> stopCurrent(
            @PathVariable String userId,
            @Valid @RequestBody(required = false) StopFocusSessionRequest req
    ) {
        FocusSession current = focusSessionService.getCurrentRunning(userId);
        if (current == null) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(focusSessionMapper.toResponse(focusSessionService.stop(userId, current.getId(), req)));
    }

    @GetMapping("/current")
    public ResponseEntity<FocusSessionResponse> getCurrent(@PathVariable String userId) {
        FocusSession current = focusSessionService.getCurrentRunning(userId);
        if (current == null) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(focusSessionMapper.toResponse(current));
    }

    @GetMapping("/running")
    public ResponseEntity<List<FocusSessionResponse>> getAllRunning(@PathVariable String userId) {
        List<FocusSessionResponse> out = focusSessionService.getAllRunning(userId)
                .stream().map(focusSessionMapper::toResponse).toList();
        return ResponseEntity.ok(out);
    }

    @GetMapping
    public ResponseEntity<List<FocusSessionResponse>> list(
            @PathVariable String userId,
            @RequestParam(required = false) String taskId
    ) {
        List<FocusSessionResponse> out = focusSessionService.listByUser(userId, taskId)
                .stream().map(focusSessionMapper::toResponse).toList();
        return ResponseEntity.ok(out);
    }
}
