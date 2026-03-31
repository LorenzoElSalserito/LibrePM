package com.lorenzodm.librepm.api.controller;

import com.lorenzodm.librepm.api.dto.response.RemoteIdentityResponse;
import com.lorenzodm.librepm.core.entity.RemoteIdentity;
import com.lorenzodm.librepm.service.RemoteIdentityService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controller for remote identity management (OIDC/OAuth2 bindings).
 * Bind/unbind endpoints are prepared for future SSO integration.
 */
@RestController
@RequestMapping("/api/users/{userId}/identities")
public class RemoteIdentityController {

    private final RemoteIdentityService remoteIdentityService;

    public RemoteIdentityController(RemoteIdentityService remoteIdentityService) {
        this.remoteIdentityService = remoteIdentityService;
    }

    @GetMapping
    public ResponseEntity<List<RemoteIdentityResponse>> list(@PathVariable String userId) {
        List<RemoteIdentityResponse> out = remoteIdentityService.listByUser(userId).stream()
                .map(this::toResponse)
                .toList();
        return ResponseEntity.ok(out);
    }

    @PostMapping
    public ResponseEntity<RemoteIdentityResponse> bind(
            @PathVariable String userId,
            @RequestBody BindRequest req) {
        RemoteIdentity identity = remoteIdentityService.bind(
                userId,
                RemoteIdentity.Provider.valueOf(req.provider()),
                req.providerUserId(),
                req.email()
        );
        return ResponseEntity.ok(toResponse(identity));
    }

    @DeleteMapping("/{identityId}")
    public ResponseEntity<Void> unbind(
            @PathVariable String userId,
            @PathVariable String identityId) {
        remoteIdentityService.unbind(identityId, userId);
        return ResponseEntity.noContent().build();
    }

    private RemoteIdentityResponse toResponse(RemoteIdentity ri) {
        return new RemoteIdentityResponse(
                ri.getId(),
                ri.getProvider().name(),
                ri.getProviderUserId(),
                ri.getEmail(),
                ri.getBoundAt()
        );
    }

    public record BindRequest(String provider, String providerUserId, String email) {}
}
