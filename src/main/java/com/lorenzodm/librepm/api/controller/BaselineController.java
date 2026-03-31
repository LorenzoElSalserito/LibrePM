package com.lorenzodm.librepm.api.controller;

import com.lorenzodm.librepm.api.dto.request.CreateBaselineRequest;
import com.lorenzodm.librepm.api.dto.response.BaselineResponse;
import com.lorenzodm.librepm.api.dto.response.VarianceResponse;
import com.lorenzodm.librepm.api.mapper.BaselineMapper;
import com.lorenzodm.librepm.service.BaselineService;
import com.lorenzodm.librepm.service.VarianceService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/users/{userId}/projects/{projectId}/baselines")
public class BaselineController {

    private final BaselineService baselineService;
    private final VarianceService varianceService;
    private final BaselineMapper baselineMapper;

    public BaselineController(BaselineService baselineService,
                              VarianceService varianceService,
                              BaselineMapper baselineMapper) {
        this.baselineService = baselineService;
        this.varianceService = varianceService;
        this.baselineMapper = baselineMapper;
    }

    @PostMapping
    public ResponseEntity<BaselineResponse> create(
            @PathVariable String userId, @PathVariable String projectId,
            @Valid @RequestBody CreateBaselineRequest req
    ) {
        var created = baselineService.create(userId, projectId, req);
        return ResponseEntity.created(URI.create(
                "/api/users/" + userId + "/projects/" + projectId + "/baselines/" + created.getId()))
                .body(baselineMapper.toResponse(created));
    }

    @GetMapping
    public ResponseEntity<List<BaselineResponse>> list(
            @PathVariable String userId, @PathVariable String projectId
    ) {
        return ResponseEntity.ok(baselineService.listByProject(userId, projectId)
                .stream().map(baselineMapper::toResponse).toList());
    }

    @GetMapping("/{baselineId}")
    public ResponseEntity<BaselineResponse> get(
            @PathVariable String userId, @PathVariable String projectId,
            @PathVariable String baselineId
    ) {
        return ResponseEntity.ok(baselineMapper.toResponse(
                baselineService.getById(userId, projectId, baselineId)));
    }

    @DeleteMapping("/{baselineId}")
    public ResponseEntity<Void> delete(
            @PathVariable String userId, @PathVariable String projectId,
            @PathVariable String baselineId
    ) {
        baselineService.delete(userId, projectId, baselineId);
        return ResponseEntity.noContent().build();
    }

    // --- Compare ---

    @GetMapping("/{baselineId}/compare")
    public ResponseEntity<Map<String, Object>> compare(
            @PathVariable String userId, @PathVariable String projectId,
            @PathVariable String baselineId
    ) {
        return ResponseEntity.ok(baselineService.compare(userId, projectId, baselineId));
    }

    // --- Variance ---

    @GetMapping("/{baselineId}/variance")
    public ResponseEntity<VarianceResponse> getVariance(
            @PathVariable String userId, @PathVariable String projectId,
            @PathVariable String baselineId
    ) {
        return ResponseEntity.ok(varianceService.calculateVariance(userId, projectId, baselineId));
    }

    @GetMapping("/latest/variance")
    public ResponseEntity<VarianceResponse> getLatestVariance(
            @PathVariable String userId, @PathVariable String projectId
    ) {
        return ResponseEntity.ok(varianceService.calculateLatestVariance(userId, projectId));
    }
}
