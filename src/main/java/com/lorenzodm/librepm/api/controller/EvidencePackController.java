package com.lorenzodm.librepm.api.controller;

import com.lorenzodm.librepm.core.entity.EvidencePack;
import com.lorenzodm.librepm.core.entity.EvidencePackItem;
import com.lorenzodm.librepm.service.EvidencePackService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/projects/{projectId}/evidence-packs")
public class EvidencePackController {

    private final EvidencePackService evidencePackService;

    public EvidencePackController(EvidencePackService evidencePackService) {
        this.evidencePackService = evidencePackService;
    }

    @GetMapping
    public ResponseEntity<List<Map<String, Object>>> list(@PathVariable String projectId) {
        return ResponseEntity.ok(
                evidencePackService.listByProject(projectId).stream()
                        .map(this::toMap)
                        .toList()
        );
    }

    @PostMapping
    public ResponseEntity<Map<String, Object>> create(
            @PathVariable String projectId,
            @RequestBody Map<String, String> body,
            @RequestHeader(value = "X-User-Id", required = false) String userId) {
        EvidencePack pack = evidencePackService.create(
                body.get("name"),
                body.get("description"),
                projectId,
                body.get("packType"),
                userId
        );
        return ResponseEntity.ok(toMap(pack));
    }

    @PatchMapping("/{packId}/finalize")
    public ResponseEntity<Map<String, Object>> finalize(@PathVariable String packId) {
        return ResponseEntity.ok(toMap(evidencePackService.finalize(packId)));
    }

    @GetMapping("/{packId}/items")
    public ResponseEntity<List<Map<String, Object>>> listItems(@PathVariable String packId) {
        return ResponseEntity.ok(
                evidencePackService.listItems(packId).stream()
                        .map(this::itemToMap)
                        .toList()
        );
    }

    @PostMapping("/{packId}/items")
    public ResponseEntity<Map<String, Object>> addItem(
            @PathVariable String packId,
            @RequestBody Map<String, Object> body) {
        String assetId = (String) body.get("assetId");
        String noteId = (String) body.get("noteId");
        int order = body.containsKey("order") ? ((Number) body.get("order")).intValue() : 0;

        EvidencePackItem item;
        if (assetId != null) {
            item = evidencePackService.addAssetItem(packId, assetId, order);
        } else {
            item = evidencePackService.addNoteItem(packId, noteId, order);
        }
        return ResponseEntity.ok(itemToMap(item));
    }

    @DeleteMapping("/{packId}/items/{itemId}")
    public ResponseEntity<Void> removeItem(@PathVariable String itemId) {
        evidencePackService.removeItem(itemId);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{packId}")
    public ResponseEntity<Void> delete(@PathVariable String packId) {
        evidencePackService.delete(packId);
        return ResponseEntity.noContent().build();
    }

    private Map<String, Object> toMap(EvidencePack p) {
        return Map.of(
                "id", p.getId(),
                "name", p.getName(),
                "description", p.getDescription() != null ? p.getDescription() : "",
                "packType", p.getPackType() != null ? p.getPackType().name() : "",
                "status", p.getStatus().name(),
                "createdAt", p.getCreatedAt() != null ? p.getCreatedAt().toString() : "",
                "finalizedAt", p.getFinalizedAt() != null ? p.getFinalizedAt().toString() : ""
        );
    }

    private Map<String, Object> itemToMap(EvidencePackItem item) {
        return Map.of(
                "id", item.getId(),
                "assetId", item.getAsset() != null ? item.getAsset().getId() : "",
                "assetName", item.getAsset() != null ? item.getAsset().getFileName() : "",
                "noteId", item.getNote() != null ? item.getNote().getId() : "",
                "noteTitle", item.getNote() != null ? item.getNote().getTitle() : "",
                "itemOrder", item.getItemOrder()
        );
    }
}
