package com.lorenzodm.librepm.api.controller;

import com.lorenzodm.librepm.api.dto.request.CreateRiskRegisterEntryRequest;
import com.lorenzodm.librepm.api.dto.request.UpdateRiskRegisterEntryRequest;
import com.lorenzodm.librepm.api.dto.response.RiskRegisterEntryResponse;
import com.lorenzodm.librepm.api.mapper.RiskRegisterEntryMapper;
import com.lorenzodm.librepm.service.RiskRegisterEntryService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/users/{userId}/projects/{projectId}/risks")
public class RiskRegisterController {

    private final RiskRegisterEntryService riskService;
    private final RiskRegisterEntryMapper riskMapper;

    public RiskRegisterController(RiskRegisterEntryService riskService, RiskRegisterEntryMapper riskMapper) {
        this.riskService = riskService;
        this.riskMapper = riskMapper;
    }

    @PostMapping
    public ResponseEntity<RiskRegisterEntryResponse> create(
            @PathVariable String userId, @PathVariable String projectId,
            @Valid @RequestBody CreateRiskRegisterEntryRequest req
    ) {
        var created = riskService.create(userId, projectId, req);
        return ResponseEntity.created(URI.create("/api/users/" + userId + "/projects/" + projectId + "/risks/" + created.getId()))
                .body(riskMapper.toResponse(created));
    }

    @GetMapping
    public ResponseEntity<List<RiskRegisterEntryResponse>> list(@PathVariable String userId, @PathVariable String projectId) {
        return ResponseEntity.ok(riskService.listByProject(userId, projectId)
                .stream().map(riskMapper::toResponse).toList());
    }

    @GetMapping("/{entryId}")
    public ResponseEntity<RiskRegisterEntryResponse> get(
            @PathVariable String userId, @PathVariable String projectId, @PathVariable String entryId
    ) {
        return ResponseEntity.ok(riskMapper.toResponse(riskService.getById(userId, projectId, entryId)));
    }

    @PutMapping("/{entryId}")
    public ResponseEntity<RiskRegisterEntryResponse> update(
            @PathVariable String userId, @PathVariable String projectId, @PathVariable String entryId,
            @Valid @RequestBody UpdateRiskRegisterEntryRequest req
    ) {
        return ResponseEntity.ok(riskMapper.toResponse(riskService.update(userId, projectId, entryId, req)));
    }

    @DeleteMapping("/{entryId}")
    public ResponseEntity<Void> delete(
            @PathVariable String userId, @PathVariable String projectId, @PathVariable String entryId
    ) {
        riskService.delete(userId, projectId, entryId);
        return ResponseEntity.noContent().build();
    }
}
