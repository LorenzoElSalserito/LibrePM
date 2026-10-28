package com.lorenzodm.librepm.service.impl;

import com.lorenzodm.librepm.api.dto.request.CreateAssetRequest;
import com.lorenzodm.librepm.api.dto.request.UpdateAssetRequest;
import com.lorenzodm.librepm.api.exception.BadRequestException;
import com.lorenzodm.librepm.api.exception.ForbiddenException;
import com.lorenzodm.librepm.api.exception.OwnershipViolationException;
import com.lorenzodm.librepm.api.exception.ResourceNotFoundException;
import com.lorenzodm.librepm.core.entity.Asset;
import com.lorenzodm.librepm.core.entity.Notification;
import com.lorenzodm.librepm.core.entity.Project;
import com.lorenzodm.librepm.core.entity.ProjectMember;
import com.lorenzodm.librepm.core.entity.Task;
import com.lorenzodm.librepm.core.entity.User;
import com.lorenzodm.librepm.repository.AssetRepository;
import com.lorenzodm.librepm.repository.ProjectMemberRepository;
import com.lorenzodm.librepm.repository.ProjectRepository;
import com.lorenzodm.librepm.repository.TaskRepository;
import com.lorenzodm.librepm.repository.UserRepository;
import com.lorenzodm.librepm.service.AssetContentStore;
import com.lorenzodm.librepm.service.AssetService;
import com.lorenzodm.librepm.service.FileStorageService;
import com.lorenzodm.librepm.service.NotificationService;
import com.lorenzodm.librepm.service.PermissionChecker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.security.DigestOutputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.HexFormat;
import java.util.List;
import java.util.Map;

/**
 * Attachments shared through the database.
 * <p>
 * Uploads are written to the local assets folder and, in the same transaction, to
 * {@code asset_contents}: any backend connected to the same database can therefore serve
 * them. The local folder is a cache, filled on first download on the other PCs. Assets
 * created before contents were shared (bytes only on the uploader's disk) are copied into
 * the database lazily on download and by {@link #backfillLocalContents()} at startup.
 * </p>
 * Access: the uploader, or any member of the attachment's project, can read; uploading to
 * a project needs write access; editing and deleting stay reserved to the uploader.
 */
@Service
@Transactional
public class AssetServiceImpl implements AssetService {

    private static final Logger log = LoggerFactory.getLogger(AssetServiceImpl.class);

    private final AssetRepository assetRepository;
    private final UserRepository userRepository;
    private final TaskRepository taskRepository;
    private final ProjectRepository projectRepository;
    private final ProjectMemberRepository projectMemberRepository;
    private final FileStorageService fileStorageService;
    private final AssetContentStore contentStore;
    private final PermissionChecker permissionChecker;
    private final NotificationService notificationService;
    private final TransactionTemplate transactions;

    public AssetServiceImpl(AssetRepository assetRepository, UserRepository userRepository, TaskRepository taskRepository,
                            ProjectRepository projectRepository, ProjectMemberRepository projectMemberRepository,
                            FileStorageService fileStorageService, AssetContentStore contentStore,
                            PermissionChecker permissionChecker, NotificationService notificationService,
                            PlatformTransactionManager transactionManager) {
        this.assetRepository = assetRepository;
        this.userRepository = userRepository;
        this.taskRepository = taskRepository;
        this.projectRepository = projectRepository;
        this.projectMemberRepository = projectMemberRepository;
        this.fileStorageService = fileStorageService;
        this.contentStore = contentStore;
        this.permissionChecker = permissionChecker;
        this.notificationService = notificationService;
        this.transactions = new TransactionTemplate(transactionManager);
    }

    @Override
    public Asset createMetadata(String userId, CreateAssetRequest req) {
        User owner = requireUser(userId);

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
        if (taskId != null && !taskId.isBlank()) {
            return uploadToProject(userId, null, taskId, file, description);
        }
        // Personal asset, not bound to any project: visible to its owner only.
        return store(requireUser(userId), file, description, null, null);
    }

    @Override
    public Asset uploadToProject(String userId, String projectId, String taskId, MultipartFile file, String description) {
        User uploader = requireUser(userId);
        Task task = null;
        if (taskId != null && !taskId.isBlank()) {
            task = taskRepository.findById(taskId)
                    .orElseThrow(() -> new ResourceNotFoundException("Task non trovato: " + taskId));
            String taskProject = task.getProject() != null ? task.getProject().getId() : null;
            if (projectId != null && !projectId.equals(taskProject)) {
                throw new BadRequestException("Il task non appartiene al progetto indicato");
            }
            projectId = taskProject;
        }
        if (projectId == null) {
            throw new BadRequestException("Progetto mancante per l'allegato");
        }
        String targetProject = projectId;
        Project project = projectRepository.findById(targetProject)
                .orElseThrow(() -> new ResourceNotFoundException("Progetto non trovato: " + targetProject));
        permissionChecker.requirePermission(userId, targetProject, "PROJECT_WRITE");

        Asset saved = store(uploader, file, description, targetProject, task);
        notifyMembers(saved, uploader, project, task);
        return saved;
    }

    private Asset store(User owner, MultipartFile file, String description, String projectId, Task task) {
        FileStorageService.StoredFile stored = fileStorageService.store(file);
        try {
            Asset a = new Asset();
            a.setFileName(stored.originalFileName());
            a.setFilePath(stored.relativePath());
            a.setMimeType(stored.mimeType());
            a.setSizeBytes(stored.sizeBytes());
            a.setChecksum(stored.checksumSha256());
            a.setDescription(description);
            a.setOwner(owner);
            a.setTask(task);
            a.setProjectId(projectId);
            Asset saved = assetRepository.saveAndFlush(a);
            // Same transaction: metadata and bytes become visible to other PCs together.
            contentStore.save(saved.getId(), stored.absolutePath());
            return saved;
        } catch (IOException | RuntimeException error) {
            deleteQuietly(stored.absolutePath());
            if (error instanceof RuntimeException runtime) throw runtime;
            throw new IllegalStateException("Errore salvataggio allegato", error);
        }
    }

    private void notifyMembers(Asset asset, User uploader, Project project, Task task) {
        String target = task != null
                ? "al task \"" + task.getTitle() + "\""
                : "al progetto \"" + project.getName() + "\"";
        String message = uploader.getDisplayName() + " ha aggiunto il file \"" + asset.getFileName() + "\" " + target;
        if (message.length() > 500) message = message.substring(0, 497) + "...";
        String refType = task != null ? "TASK" : "PROJECT";
        String refId = task != null ? task.getId() : project.getId();
        for (ProjectMember member : projectMemberRepository.findByProjectId(project.getId())) {
            if (member.getUser() != null && !member.getUser().getId().equals(uploader.getId())) {
                notificationService.create(member.getUser(), uploader, Notification.NotificationType.FILE_ADDED,
                        message, refType, refId);
            }
        }
    }

    @Override
    public Asset getOwned(String userId, String assetId) {
        Asset a = requireAsset(assetId);
        if (a.getOwner() == null || !userId.equals(a.getOwner().getId())) {
            throw new OwnershipViolationException("Asset non appartiene all'utente");
        }
        return a;
    }

    @Override
    public Asset getAccessible(String userId, String assetId) {
        Asset a = requireAsset(assetId);
        if (a.getOwner() != null && userId.equals(a.getOwner().getId())) return a;
        String projectId = projectIdOf(a);
        if (projectId != null && permissionChecker.hasPermission(userId, projectId, "PROJECT_READ")) return a;
        throw new ForbiddenException("Non hai accesso a questo allegato");
    }

    @Override
    public AssetContent download(String userId, String assetId) {
        Asset asset = getAccessible(userId, assetId);
        Resource resource = resolveContent(asset);
        asset.markAsAccessed();
        return new AssetContent(assetRepository.save(asset), resource);
    }

    private Resource resolveContent(Asset asset) {
        Path local = fileStorageService.resolveLocal(asset.getFilePath());
        boolean onDisk = Files.isRegularFile(local);
        if (contentStore.exists(asset.getId())) {
            if (onDisk && sizeMatches(local, asset)) return new FileSystemResource(local);
            return materialize(asset, local);
        }
        if (onDisk) {
            // Legacy asset, bytes only on this PC: share them now.
            try {
                contentStore.save(asset.getId(), local);
            } catch (IOException | DataAccessException error) {
                log.warn("Backfill of asset {} into the database failed: {}", asset.getId(), error.getMessage());
            }
            return new FileSystemResource(local);
        }
        throw new ResourceNotFoundException("Il contenuto del file \"" + asset.getFileName() + "\" non è disponibile: " +
                "è stato caricato prima della condivisione dei file e il PC di chi l'ha caricato non l'ha ancora sincronizzato");
    }

    /** Copies the bytes from the database into the local cache, verifying the checksum. */
    private Resource materialize(Asset asset, Path local) {
        Path temp = null;
        try {
            Files.createDirectories(local.getParent());
            temp = Files.createTempFile(local.getParent(), ".download-", ".part");
            MessageDigest digest = sha256();
            try (OutputStream out = new DigestOutputStream(Files.newOutputStream(temp), digest)) {
                contentStore.copyTo(asset.getId(), out);
            }
            verifyChecksum(asset, digest);
            try {
                Files.move(temp, local, StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING);
            } catch (AtomicMoveNotSupportedException unsupported) {
                Files.move(temp, local, StandardCopyOption.REPLACE_EXISTING);
            }
            return new FileSystemResource(local);
        } catch (IOException cacheUnavailable) {
            deleteQuietly(temp);
            log.warn("Local cache unavailable for asset {}, serving from database: {}", asset.getId(), cacheUnavailable.getMessage());
            ByteArrayOutputStream buffer = new ByteArrayOutputStream();
            MessageDigest digest = sha256();
            try (OutputStream out = new DigestOutputStream(buffer, digest)) {
                contentStore.copyTo(asset.getId(), out);
            } catch (IOException error) {
                throw new IllegalStateException("Lettura allegato dal database fallita", error);
            }
            verifyChecksum(asset, digest);
            return new ByteArrayResource(buffer.toByteArray());
        } catch (RuntimeException error) {
            deleteQuietly(temp);
            throw error;
        }
    }

    private void verifyChecksum(Asset asset, MessageDigest digest) {
        String actual = HexFormat.of().formatHex(digest.digest());
        if (asset.getChecksum() != null && !asset.getChecksum().isBlank() && !asset.getChecksum().equalsIgnoreCase(actual)) {
            throw new IllegalStateException("Checksum allegato non valido: " + asset.getId());
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<Asset> listOwned(String userId, boolean includeDeleted) {
        return includeDeleted ? assetRepository.findByOwnerId(userId) : assetRepository.findByOwnerIdAndDeletedAtIsNull(userId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Asset> listForProject(String userId, String projectId) {
        if (!projectRepository.existsById(projectId)) throw new ResourceNotFoundException("Progetto non trovato: " + projectId);
        permissionChecker.requirePermission(userId, projectId, "PROJECT_READ");
        return assetRepository.findActiveByProject(projectId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Asset> listForTask(String userId, String taskId) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new ResourceNotFoundException("Task non trovato: " + taskId));
        if (task.getProject() == null) throw new ForbiddenException("Non hai accesso a questo task");
        permissionChecker.requirePermission(userId, task.getProject().getId(), "PROJECT_READ");
        return assetRepository.findActiveByTask(taskId);
    }

    @Override
    public Asset update(String userId, String assetId, UpdateAssetRequest req) {
        Asset a = getOwned(userId, assetId);

        if (req.description() != null) a.setDescription(req.description());
        if (req.deleted() != null) {
            applyDeleted(a, req.deleted());
        }

        return assetRepository.save(a);
    }

    @Override
    public Asset setDeleted(String userId, String assetId, boolean deleted) {
        Asset a = getOwned(userId, assetId);
        applyDeleted(a, deleted);
        return assetRepository.save(a);
    }

    private void applyDeleted(Asset a, boolean deleted) {
        a.setDeletedAt(deleted ? Instant.now() : null);
        if (deleted) {
            // Deleted assets are no longer reachable: free the shared database and the local cache.
            contentStore.delete(a.getId());
            try {
                deleteQuietly(fileStorageService.resolveLocal(a.getFilePath()));
            } catch (RuntimeException ignored) {
                // An invalid legacy path leaves nothing to clean up.
            }
        }
    }

    @Override
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    public int backfillLocalContents() {
        int copied = 0;
        for (Map.Entry<String, String> missing : contentStore.missingContents().entrySet()) {
            Path local;
            try {
                local = fileStorageService.resolveLocal(missing.getValue());
            } catch (RuntimeException invalidPath) {
                continue;
            }
            if (!Files.isRegularFile(local)) continue;
            try {
                transactions.executeWithoutResult(status -> {
                    try {
                        if (!contentStore.exists(missing.getKey())) contentStore.save(missing.getKey(), local);
                    } catch (IOException error) {
                        throw new IllegalStateException(error);
                    }
                });
                copied++;
            } catch (RuntimeException error) {
                log.warn("Backfill of asset {} into the database failed: {}", missing.getKey(), error.getMessage());
            }
        }
        return copied;
    }

    private String projectIdOf(Asset a) {
        if (a.getProjectId() != null) return a.getProjectId();
        if (a.getTask() != null && a.getTask().getProject() != null) return a.getTask().getProject().getId();
        return null;
    }

    private User requireUser(String userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User non trovato: " + userId));
    }

    private Asset requireAsset(String assetId) {
        return assetRepository.findById(assetId)
                .orElseThrow(() -> new ResourceNotFoundException("Asset non trovato: " + assetId));
    }

    private static boolean sizeMatches(Path file, Asset asset) {
        try {
            return asset.getSizeBytes() == null || Files.size(file) == asset.getSizeBytes();
        } catch (IOException e) {
            return false;
        }
    }

    private static MessageDigest sha256() {
        try {
            return MessageDigest.getInstance("SHA-256");
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }

    private static void deleteQuietly(Path path) {
        if (path == null) return;
        try {
            Files.deleteIfExists(path);
        } catch (IOException ignored) {
            // Best effort: a stale cache file is harmless.
        }
    }
}
