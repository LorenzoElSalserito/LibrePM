package com.lorenzodm.librepm.api.controller;

import com.lorenzodm.librepm.api.dto.request.CreateDeliverableRequest;
import com.lorenzodm.librepm.api.dto.request.UpdateDeliverableRequest;
import com.lorenzodm.librepm.api.dto.response.DeliverableResponse;
import com.lorenzodm.librepm.api.mapper.DeliverableMapper;
import com.lorenzodm.librepm.service.DeliverableService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/users/{userId}/projects/{projectId}/deliverables")
public class DeliverableController {

    private final DeliverableService deliverableService;
    private final DeliverableMapper deliverableMapper;

    public DeliverableController(DeliverableService deliverableService, DeliverableMapper deliverableMapper) {
        this.deliverableService = deliverableService;
        this.deliverableMapper = deliverableMapper;
    }

    @PostMapping
    public ResponseEntity<DeliverableResponse> create(
            @PathVariable String userId, @PathVariable String projectId,
            @Valid @RequestBody CreateDeliverableRequest req
    ) {
        var created = deliverableService.create(userId, projectId, req);
        return ResponseEntity.created(URI.create("/api/users/" + userId + "/projects/" + projectId + "/deliverables/" + created.getId()))
                .body(deliverableMapper.toResponse(created));
    }

    @GetMapping
    public ResponseEntity<List<DeliverableResponse>> list(@PathVariable String userId, @PathVariable String projectId) {
        return ResponseEntity.ok(deliverableService.listByProject(userId, projectId)
                .stream().map(deliverableMapper::toResponse).toList());
    }

    @GetMapping("/{deliverableId}")
    public ResponseEntity<DeliverableResponse> get(
            @PathVariable String userId, @PathVariable String projectId, @PathVariable String deliverableId
    ) {
        return ResponseEntity.ok(deliverableMapper.toResponse(deliverableService.getById(userId, projectId, deliverableId)));
    }

    @PutMapping("/{deliverableId}")
    public ResponseEntity<DeliverableResponse> update(
            @PathVariable String userId, @PathVariable String projectId, @PathVariable String deliverableId,
            @Valid @RequestBody UpdateDeliverableRequest req
    ) {
        return ResponseEntity.ok(deliverableMapper.toResponse(deliverableService.update(userId, projectId, deliverableId, req)));
    }

    @DeleteMapping("/{deliverableId}")
    public ResponseEntity<Void> delete(
            @PathVariable String userId, @PathVariable String projectId, @PathVariable String deliverableId
    ) {
        deliverableService.delete(userId, projectId, deliverableId);
        return ResponseEntity.noContent().build();
    }
}
