package com.lorenzodm.librepm.api.controller;

import com.lorenzodm.librepm.api.dto.request.AddSuccessMetricRequest;
import com.lorenzodm.librepm.api.dto.request.CreateOkrRequest;
import com.lorenzodm.librepm.api.dto.request.RecordAchievementRequest;
import com.lorenzodm.librepm.api.dto.request.UpdateSuccessMetricRequest;
import com.lorenzodm.librepm.api.dto.response.OkrResponse;
import com.lorenzodm.librepm.api.dto.response.SuccessMetricResponse;
import com.lorenzodm.librepm.api.dto.response.TargetAchievedRecordResponse;
import com.lorenzodm.librepm.api.mapper.OkrMapper;
import com.lorenzodm.librepm.service.OkrService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/users/{userId}/projects/{projectId}/okrs")
public class OkrController {

    private final OkrService okrService;
    private final OkrMapper okrMapper;

    public OkrController(OkrService okrService, OkrMapper okrMapper) {
        this.okrService = okrService;
        this.okrMapper = okrMapper;
    }

    @PostMapping
    public ResponseEntity<OkrResponse> create(
            @PathVariable String userId, @PathVariable String projectId,
            @Valid @RequestBody CreateOkrRequest req
    ) {
        var created = okrService.create(userId, projectId, req);
        return ResponseEntity.created(URI.create("/api/users/" + userId + "/projects/" + projectId + "/okrs/" + created.getId()))
                .body(okrMapper.toResponse(created));
    }

    @GetMapping
    public ResponseEntity<List<OkrResponse>> list(@PathVariable String userId, @PathVariable String projectId) {
        return ResponseEntity.ok(okrService.listByProject(userId, projectId)
                .stream().map(okrMapper::toResponse).toList());
    }

    @GetMapping("/{okrId}")
    public ResponseEntity<OkrResponse> get(
            @PathVariable String userId, @PathVariable String projectId, @PathVariable String okrId
    ) {
        return ResponseEntity.ok(okrMapper.toResponse(okrService.getById(userId, projectId, okrId)));
    }

    @DeleteMapping("/{okrId}")
    public ResponseEntity<Void> delete(
            @PathVariable String userId, @PathVariable String projectId, @PathVariable String okrId
    ) {
        okrService.delete(userId, projectId, okrId);
        return ResponseEntity.noContent().build();
    }

    // --- Key Results (SuccessMetrics) ---

    @PostMapping("/{okrId}/key-results")
    public ResponseEntity<SuccessMetricResponse> addKeyResult(
            @PathVariable String userId, @PathVariable String projectId, @PathVariable String okrId,
            @Valid @RequestBody AddSuccessMetricRequest req
    ) {
        return ResponseEntity.ok(okrMapper.toMetricResponse(
                okrService.addKeyResult(userId, projectId, okrId, req)));
    }

    @PutMapping("/{okrId}/key-results/{metricId}")
    public ResponseEntity<SuccessMetricResponse> updateKeyResult(
            @PathVariable String userId, @PathVariable String projectId,
            @PathVariable String okrId, @PathVariable String metricId,
            @Valid @RequestBody UpdateSuccessMetricRequest req
    ) {
        return ResponseEntity.ok(okrMapper.toMetricResponse(
                okrService.updateKeyResult(userId, projectId, okrId, metricId, req)));
    }

    @DeleteMapping("/{okrId}/key-results/{metricId}")
    public ResponseEntity<Void> removeKeyResult(
            @PathVariable String userId, @PathVariable String projectId,
            @PathVariable String okrId, @PathVariable String metricId
    ) {
        okrService.removeKeyResult(userId, projectId, okrId, metricId);
        return ResponseEntity.noContent().build();
    }

    // --- Achievement History ---

    @PostMapping("/{okrId}/key-results/{metricId}/achievements")
    public ResponseEntity<TargetAchievedRecordResponse> recordAchievement(
            @PathVariable String userId, @PathVariable String projectId,
            @PathVariable String okrId, @PathVariable String metricId,
            @Valid @RequestBody RecordAchievementRequest req
    ) {
        return ResponseEntity.ok(okrMapper.toRecordResponse(
                okrService.recordAchievement(userId, projectId, okrId, metricId, req)));
    }

    @GetMapping("/{okrId}/key-results/{metricId}/achievements")
    public ResponseEntity<List<TargetAchievedRecordResponse>> getAchievements(
            @PathVariable String userId, @PathVariable String projectId,
            @PathVariable String okrId, @PathVariable String metricId
    ) {
        return ResponseEntity.ok(okrService.getAchievementHistory(userId, projectId, okrId, metricId)
                .stream().map(okrMapper::toRecordResponse).toList());
    }
}
