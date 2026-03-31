package com.lorenzodm.librepm.repository;

import com.lorenzodm.librepm.core.entity.AssetLink;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AssetLinkRepository extends JpaRepository<AssetLink, String> {

    List<AssetLink> findByLinkedEntityTypeAndLinkedEntityId(String entityType, String entityId);

    List<AssetLink> findByAssetId(String assetId);

    void deleteByAssetIdAndLinkedEntityTypeAndLinkedEntityId(String assetId, String entityType, String entityId);
}
