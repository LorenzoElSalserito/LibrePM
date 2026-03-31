package com.lorenzodm.librepm.api.controller;

import com.lorenzodm.librepm.api.dto.request.CreateExternalContributorRequest;
import com.lorenzodm.librepm.api.dto.response.ExternalContributorResponse;
import com.lorenzodm.librepm.core.entity.ExternalContributor;
import com.lorenzodm.librepm.service.ExternalContributorService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users/{userId}/external-contributors")
public class ExternalContributorController {

    private final ExternalContributorService service;

    public ExternalContributorController(ExternalContributorService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<ExternalContributorResponse> create(
            @PathVariable String userId,
            @Valid @RequestBody CreateExternalContributorRequest req) {
        ExternalContributor ec = service.create(userId, req);
        return ResponseEntity.status(HttpStatus.CREATED).body(toResponse(ec));
    }

    @GetMapping
    public ResponseEntity<List<ExternalContributorResponse>> listAll(@PathVariable String userId) {
        List<ExternalContributorResponse> response = service.listAll().stream()
                .map(this::toResponse)
                .toList();
        return ResponseEntity.ok(response);
    }

    @GetMapping("/by-scope/{scopeEntityId}")
    public ResponseEntity<List<ExternalContributorResponse>> listByScope(
            @PathVariable String userId,
            @PathVariable String scopeEntityId) {
        List<ExternalContributorResponse> response = service.listByScope(scopeEntityId).stream()
                .map(this::toResponse)
                .toList();
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{contributorId}")
    public ResponseEntity<Void> revoke(
            @PathVariable String userId,
            @PathVariable String contributorId) {
        service.revoke(userId, contributorId);
        return ResponseEntity.noContent().build();
    }

    private ExternalContributorResponse toResponse(ExternalContributor ec) {
        return new ExternalContributorResponse(
                ec.getId(),
                ec.getDisplayName(),
                ec.getEmail(),
                ec.getOrganization(),
                ec.getRole() != null ? ec.getRole().getId() : null,
                ec.getRole() != null ? ec.getRole().getName() : null,
                ec.getScope().name(),
                ec.getScopeEntityId(),
                ec.getAccessExpiresAt(),
                ec.getCreatedAt(),
                ec.getCreatedBy() != null ? ec.getCreatedBy().getId() : null
        );
    }
}
