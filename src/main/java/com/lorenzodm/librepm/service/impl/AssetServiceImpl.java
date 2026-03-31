package com.lorenzodm.librepm.service.impl;

import com.lorenzodm.librepm.api.dto.request.CreateAssetRequest;
import com.lorenzodm.librepm.api.dto.request.UpdateAssetRequest;
import com.lorenzodm.librepm.api.exception.ResourceNotFoundException;
import com.lorenzodm.librepm.api.exception.OwnershipViolationException;
import com.lorenzodm.librepm.core.entity.Asset;
import com.lorenzodm.librepm.core.entity.Task;
import com.lorenzodm.librepm.core.entity.User;
import com.lorenzodm.librepm.repository.AssetRepository;
import com.lorenzodm.librepm.repository.TaskRepository;
import com.lorenzodm.librepm.repository.UserRepository;
import com.lorenzodm.librepm.service.AssetService;
import com.lorenzodm.librepm.service.FileStorageService;
import jakarta.transaction.Transactional;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.time.Instant;
import java.util.List;

@Service
@Transactional
public class AssetServiceImpl implements AssetService {

    private final AssetRepository assetRepository;
    private final UserRepository userRepository;
    private final TaskRepository taskRepository;
    private final FileStorageService fileStorageService;

    public AssetServiceImpl(AssetRepository assetRepository, UserRepository userRepository, TaskRepository taskRepository, FileStorageService fileStorageService) {
        this.assetRepository = assetRepository;
        this.userRepository = userRepository;
        this.taskRepository = taskRepository;
        this.fileStorageService = fileStorageService;
    }

    @Override
    public Asset createMetadata(String userId, CreateAssetRequest req) {
        User owner = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User non trovato: " + userId));

        Asset a = new Asset();
        a.setFileName(req.fileName());
        a.setFilePath(req.filePath());
        a.setMimeType(req.mimeType());
        a.setSizeBytes(req.sizeBytes());
        a.setChecksum(req.checksum());
        a.setDescription(req.description());
        a.setThumbnailPath(req.thumbnailPath());
        a.setOwner(owner);

        return assetRepository.save(a);
    }

    @Override
    public Asset upload(String userId, MultipartFile file, String description) {
        return upload(userId, file, description, null);
    }

    @Override
    public Asset upload(String userId, MultipartFile file, String description, String taskId) {
        User owner = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User non trovato: " + userId));

        FileStorageService.StoredFile stored = fileStorageService.store(file);

        Asset a = new Asset();
        a.setFileName(stored.originalFileName());
        a.setFilePath(stored.relativePath());
        a.setMimeType(stored.mimeType());
        a.setSizeBytes(stored.sizeBytes());
        a.setChecksum(stored.checksumSha256());
        a.setDescription(description);
        a.setOwner(owner);

        if (taskId != null && !taskId.isBlank()) {
            Task task = taskRepository.findById(taskId)
                    .orElseThrow(() -> new ResourceNotFoundException("Task non trovato: " + taskId));
            // Optional: check task ownership/access
            a.setTask(task);
        }

        return assetRepository.save(a);
    }

    @Override
    public Asset getOwned(String userId, String assetId) {
        Asset a = assetRepository.findById(assetId)
                .orElseThrow(() -> new ResourceNotFoundException("Asset non trovato: " + assetId));

        if (a.getOwner() == null || !userId.equals(a.getOwner().getId())) {
            throw new OwnershipViolationException("Asset non appartiene all'utente");
        }

        a.markAsAccessed();
        return assetRepository.save(a);
    }

    @Override
    public Resource download(String userId, String assetId) {
        Asset asset = getOwned(userId, assetId);
        return fileStorageService.loadAsResource(asset.getFilePath());
    }

    @Override
    public List<Asset> listOwned(String userId, boolean includeDeleted) {
        return includeDeleted ? assetRepository.findByOwnerId(userId) : assetRepository.findByOwnerIdAndDeletedAtIsNull(userId);
    }

    @Override
    public Asset update(String userId, String assetId, UpdateAssetRequest req) {
        Asset a = getOwned(userId, assetId);

        if (req.description() != null) a.setDescription(req.description());
        if (req.deleted() != null) {
            a.setDeletedAt(req.deleted() ? Instant.now() : null);
        }

        return assetRepository.save(a);
    }

    @Override
    public Asset setDeleted(String userId, String assetId, boolean deleted) {
        Asset a = getOwned(userId, assetId);
        a.setDeletedAt(deleted ? Instant.now() : null);
        return assetRepository.save(a);
    }
}
