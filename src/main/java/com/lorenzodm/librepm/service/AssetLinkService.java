package com.lorenzodm.librepm.service;

import com.lorenzodm.librepm.core.entity.AssetLink;

import java.util.List;

public interface AssetLinkService {

    AssetLink link(String assetId, String entityType, String entityId);

    void unlink(String linkId);

    List<AssetLink> listByEntity(String entityType, String entityId);

    List<AssetLink> listByAsset(String assetId);
}
