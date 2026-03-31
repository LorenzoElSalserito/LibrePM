package com.lorenzodm.librepm.api.controller;

import com.lorenzodm.librepm.api.dto.request.CreateAssetRequest;
import com.lorenzodm.librepm.api.dto.request.UpdateAssetRequest;
import com.lorenzodm.librepm.api.dto.response.AssetResponse;
import com.lorenzodm.librepm.api.mapper.AssetMapper;
import com.lorenzodm.librepm.core.entity.Asset;
import com.lorenzodm.librepm.service.AssetService;
import jakarta.validation.Valid;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/users/{userId}/assets")
public class AssetController {

    private final AssetService assetService;
    private final AssetMapper assetMapper;

    public AssetController(AssetService assetService, AssetMapper assetMapper) {
        this.assetService = assetService;
        this.assetMapper = assetMapper;
    }

    @PostMapping("/metadata")
    public ResponseEntity<AssetResponse> createMetadata(@PathVariable String userId, @Valid @RequestBody CreateAssetRequest req) {
        Asset created = assetService.createMetadata(userId, req);
        return ResponseEntity.created(URI.create("/api/users/" + userId + "/assets/" + created.getId()))
                .body(assetMapper.toResponse(created));
    }

    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<AssetResponse> upload(
            @PathVariable String userId,
            @RequestPart("file") MultipartFile file,
            @RequestPart(value = "description", required = false) String description,
            @RequestParam(value = "taskId", required = false) String taskId
    ) {
        Asset created = assetService.upload(userId, file, description, taskId);
        return ResponseEntity.created(URI.create("/api/users/" + userId + "/assets/" + created.getId()))
                .body(assetMapper.toResponse(created));
    }

    @GetMapping("/{assetId}")
    public ResponseEntity<AssetResponse> get(@PathVariable String userId, @PathVariable String assetId) {
        return ResponseEntity.ok(assetMapper.toResponse(assetService.getOwned(userId, assetId)));
    }

    @GetMapping("/{assetId}/download")
    public ResponseEntity<Resource> download(@PathVariable String userId, @PathVariable String assetId) {
        Asset asset = assetService.getOwned(userId, assetId);
        Resource resource = assetService.download(userId, assetId);

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(asset.getMimeType()))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + asset.getFileName() + "\"")
                .body(resource);
    }

    @GetMapping
    public ResponseEntity<List<AssetResponse>> list(
            @PathVariable String userId,
            @RequestParam(defaultValue = "false") boolean includeDeleted
    ) {
        List<AssetResponse> out = assetService.listOwned(userId, includeDeleted).stream()
                .map(assetMapper::toResponse)
                .toList();
        return ResponseEntity.ok(out);
    }

    @PutMapping("/{assetId}")
    public ResponseEntity<AssetResponse> update(@PathVariable String userId, @PathVariable String assetId, @Valid @RequestBody UpdateAssetRequest req) {
        return ResponseEntity.ok(assetMapper.toResponse(assetService.update(userId, assetId, req)));
    }

    @PatchMapping("/{assetId}/deleted")
    public ResponseEntity<AssetResponse> setDeleted(@PathVariable String userId, @PathVariable String assetId, @RequestParam boolean deleted) {
        return ResponseEntity.ok(assetMapper.toResponse(assetService.setDeleted(userId, assetId, deleted)));
    }
}
