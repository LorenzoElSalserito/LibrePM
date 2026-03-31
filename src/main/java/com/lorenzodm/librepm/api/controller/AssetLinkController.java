package com.lorenzodm.librepm.api.controller;

import com.lorenzodm.librepm.core.entity.AssetLink;
import com.lorenzodm.librepm.service.AssetLinkService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/asset-links")
public class AssetLinkController {

    private final AssetLinkService assetLinkService;

    public AssetLinkController(AssetLinkService assetLinkService) {
        this.assetLinkService = assetLinkService;
    }

    @PostMapping
    public ResponseEntity<Map<String, Object>> link(@RequestBody Map<String, String> body) {
        AssetLink link = assetLinkService.link(
                body.get("assetId"),
                body.get("entityType"),
                body.get("entityId")
        );
        return ResponseEntity.ok(toMap(link));
    }

    @DeleteMapping("/{linkId}")
    public ResponseEntity<Void> unlink(@PathVariable String linkId) {
        assetLinkService.unlink(linkId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping
    public ResponseEntity<List<Map<String, Object>>> listByEntity(
            @RequestParam String entityType,
            @RequestParam String entityId) {
        return ResponseEntity.ok(
                assetLinkService.listByEntity(entityType, entityId).stream()
                        .map(this::toMap)
                        .toList()
        );
    }

    private Map<String, Object> toMap(AssetLink l) {
        return Map.of(
                "id", l.getId(),
                "assetId", l.getAsset().getId(),
                "assetName", l.getAsset().getFileName(),
                "linkedEntityType", l.getLinkedEntityType(),
                "linkedEntityId", l.getLinkedEntityId(),
                "createdAt", l.getCreatedAt() != null ? l.getCreatedAt().toString() : ""
        );
    }
}
