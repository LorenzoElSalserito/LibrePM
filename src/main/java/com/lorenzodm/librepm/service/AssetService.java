package com.lorenzodm.librepm.service;

import com.lorenzodm.librepm.api.dto.request.CreateAssetRequest;
import com.lorenzodm.librepm.api.dto.request.UpdateAssetRequest;
import com.lorenzodm.librepm.core.entity.Asset;
import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface AssetService {
    Asset createMetadata(String userId, CreateAssetRequest req);
    Asset upload(String userId, MultipartFile file, String description);
    Asset upload(String userId, MultipartFile file, String description, String taskId);

    /**
     * Uploads a file attached to a project, or to one of its tasks when {@code taskId} is set.
     * Requires write access to the project; the other members are notified.
     */
    Asset uploadToProject(String userId, String projectId, String taskId, MultipartFile file, String description);

    /** Owner-only access (edit/delete rules). */
    Asset getOwned(String userId, String assetId);

    /** Read access: the uploader or any member of the asset's project. */
    Asset getAccessible(String userId, String assetId);

    /** Bytes of an accessible asset, from the local cache or the shared database. */
    AssetContent download(String userId, String assetId);

    List<Asset> listOwned(String userId, boolean includeDeleted);
    List<Asset> listForProject(String userId, String projectId);
    List<Asset> listForTask(String userId, String taskId);
    Asset update(String userId, String assetId, UpdateAssetRequest req);
    Asset setDeleted(String userId, String assetId, boolean deleted);

    /**
     * Copies into the database the bytes of assets that exist only on this PC's disk
     * (attachments uploaded before contents were shared).
     *
     * @return number of assets backfilled
     */
    int backfillLocalContents();

    record AssetContent(Asset asset, Resource resource) { }
}
