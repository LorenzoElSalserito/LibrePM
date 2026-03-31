package com.lorenzodm.librepm.service;

import com.lorenzodm.librepm.core.entity.AssetVersion;

import java.util.List;

public interface AssetVersionService {

    AssetVersion createVersion(String assetId, String filePath, Long fileSize,
                               String checksum, String uploadedBy, String comment);

    List<AssetVersion> listVersions(String assetId);
}
