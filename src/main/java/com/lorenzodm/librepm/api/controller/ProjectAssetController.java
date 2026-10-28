package com.lorenzodm.librepm.api.controller;

import com.lorenzodm.librepm.api.dto.response.AssetResponse;
import com.lorenzodm.librepm.api.mapper.AssetMapper;
import com.lorenzodm.librepm.core.entity.Asset;
import com.lorenzodm.librepm.service.AssetService;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.net.URI;
import java.util.List;

/**
 * Project and task attachments shared between collaborators: bytes travel through the
 * database, so a file uploaded on one PC can be listed and opened on every other PC
 * connected to the same database. Membership of the project is required.
 */
@RestController
@RequestMapping("/api")
public class ProjectAssetController {

    private final AssetService assetService;
    private final AssetMapper assetMapper;

    public ProjectAssetController(AssetService assetService, AssetMapper assetMapper) {
        this.assetService = assetService;
        this.assetMapper = assetMapper;
    }

    @GetMapping("/projects/{projectId}/assets")
    public List<AssetResponse> listForProject(@RequestHeader("X-User-Id") String userId, @PathVariable String projectId) {
        return assetService.listForProject(userId, projectId).stream().map(assetMapper::toResponse).toList();
    }

    @PostMapping(value = "/projects/{projectId}/assets", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<AssetResponse> uploadToProject(
            @RequestHeader("X-User-Id") String userId,
            @PathVariable String projectId,
            @RequestPart("file") MultipartFile file,
            @RequestParam(value = "description", required = false) String description,
            @RequestParam(value = "taskId", required = false) String taskId) {
        return created(assetService.uploadToProject(userId, projectId, taskId, file, description));
    }

    @GetMapping("/tasks/{taskId}/assets")
    public List<AssetResponse> listForTask(@RequestHeader("X-User-Id") String userId, @PathVariable String taskId) {
        return assetService.listForTask(userId, taskId).stream().map(assetMapper::toResponse).toList();
    }

    @PostMapping(value = "/tasks/{taskId}/assets", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<AssetResponse> uploadToTask(
            @RequestHeader("X-User-Id") String userId,
            @PathVariable String taskId,
            @RequestPart("file") MultipartFile file,
            @RequestParam(value = "description", required = false) String description) {
        return created(assetService.uploadToProject(userId, null, taskId, file, description));
    }

    @GetMapping("/assets/{assetId}")
    public AssetResponse get(@RequestHeader("X-User-Id") String userId, @PathVariable String assetId) {
        return assetMapper.toResponse(assetService.getAccessible(userId, assetId));
    }

    @GetMapping("/assets/{assetId}/download")
    public ResponseEntity<Resource> download(@RequestHeader("X-User-Id") String userId, @PathVariable String assetId) {
        return AssetDownloads.toResponse(assetService.download(userId, assetId));
    }

    @DeleteMapping("/assets/{assetId}")
    public ResponseEntity<Void> delete(@RequestHeader("X-User-Id") String userId, @PathVariable String assetId) {
        assetService.setDeleted(userId, assetId, true);
        return ResponseEntity.noContent().build();
    }

    private ResponseEntity<AssetResponse> created(Asset asset) {
        return ResponseEntity.created(URI.create("/api/assets/" + asset.getId())).body(assetMapper.toResponse(asset));
    }
}
