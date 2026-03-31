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
    Asset getOwned(String userId, String assetId);
    Resource download(String userId, String assetId);
    List<Asset> listOwned(String userId, boolean includeDeleted);
    Asset update(String userId, String assetId, UpdateAssetRequest req);
    Asset setDeleted(String userId, String assetId, boolean deleted);
}
