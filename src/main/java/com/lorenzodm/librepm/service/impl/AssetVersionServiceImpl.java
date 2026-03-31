package com.lorenzodm.librepm.service.impl;

import com.lorenzodm.librepm.api.exception.ResourceNotFoundException;
import com.lorenzodm.librepm.core.entity.Asset;
import com.lorenzodm.librepm.core.entity.AssetVersion;
import com.lorenzodm.librepm.core.entity.User;
import com.lorenzodm.librepm.repository.AssetRepository;
import com.lorenzodm.librepm.repository.AssetVersionRepository;
import com.lorenzodm.librepm.repository.UserRepository;
import com.lorenzodm.librepm.service.AssetVersionService;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Transactional
public class AssetVersionServiceImpl implements AssetVersionService {

    private final AssetVersionRepository versionRepository;
    private final AssetRepository assetRepository;
    private final UserRepository userRepository;

    public AssetVersionServiceImpl(AssetVersionRepository versionRepository,
                                   AssetRepository assetRepository,
                                   UserRepository userRepository) {
        this.versionRepository = versionRepository;
        this.assetRepository = assetRepository;
        this.userRepository = userRepository;
    }

    @Override
    public AssetVersion createVersion(String assetId, String filePath, Long fileSize,
                                      String checksum, String uploadedBy, String comment) {
        Asset asset = assetRepository.findById(assetId)
                .orElseThrow(() -> new ResourceNotFoundException("Asset not found: " + assetId));

        int nextVersion = versionRepository.findTopByAssetIdOrderByVersionNumberDesc(assetId)
                .map(v -> v.getVersionNumber() + 1)
                .orElse(1);

        AssetVersion version = new AssetVersion();
        version.setAsset(asset);
        version.setVersionNumber(nextVersion);
        version.setFilePath(filePath);
        version.setFileSize(fileSize);
        version.setChecksum(checksum);
        version.setComment(comment);

        if (uploadedBy != null) {
            userRepository.findById(uploadedBy).ifPresent(version::setUploadedBy);
        }

        return versionRepository.save(version);
    }

    @Override
    public List<AssetVersion> listVersions(String assetId) {
        return versionRepository.findByAssetIdOrderByVersionNumberDesc(assetId);
    }
}
