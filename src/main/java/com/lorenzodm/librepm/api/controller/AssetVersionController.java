package com.lorenzodm.librepm.api.controller;

import com.lorenzodm.librepm.core.entity.AssetVersion;
import com.lorenzodm.librepm.service.AssetVersionService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/projects/{projectId}/assets/{assetId}/versions")
public class AssetVersionController {

    private final AssetVersionService assetVersionService;

    public AssetVersionController(AssetVersionService assetVersionService) {
        this.assetVersionService = assetVersionService;
    }

    @GetMapping
    public ResponseEntity<List<Map<String, Object>>> list(@PathVariable String assetId) {
        List<Map<String, Object>> versions = assetVersionService.listVersions(assetId).stream()
                .map(this::toMap)
                .toList();
        return ResponseEntity.ok(versions);
    }

    private Map<String, Object> toMap(AssetVersion v) {
        return Map.of(
                "id", v.getId(),
                "versionNumber", v.getVersionNumber(),
                "filePath", v.getFilePath(),
                "fileSize", v.getFileSize() != null ? v.getFileSize() : 0,
                "checksum", v.getChecksum() != null ? v.getChecksum() : "",
                "uploadedBy", v.getUploadedBy() != null ? v.getUploadedBy().getDisplayName() : "",
                "uploadedAt", v.getUploadedAt() != null ? v.getUploadedAt().toString() : "",
                "comment", v.getComment() != null ? v.getComment() : ""
        );
    }
}
