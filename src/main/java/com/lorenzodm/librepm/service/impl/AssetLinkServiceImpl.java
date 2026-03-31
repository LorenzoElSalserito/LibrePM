package com.lorenzodm.librepm.service.impl;

import com.lorenzodm.librepm.api.exception.ResourceNotFoundException;
import com.lorenzodm.librepm.core.entity.AssetLink;
import com.lorenzodm.librepm.repository.AssetLinkRepository;
import com.lorenzodm.librepm.repository.AssetRepository;
import com.lorenzodm.librepm.service.AssetLinkService;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Transactional
public class AssetLinkServiceImpl implements AssetLinkService {

    private final AssetLinkRepository linkRepository;
    private final AssetRepository assetRepository;

    public AssetLinkServiceImpl(AssetLinkRepository linkRepository,
                                AssetRepository assetRepository) {
        this.linkRepository = linkRepository;
        this.assetRepository = assetRepository;
    }

    @Override
    public AssetLink link(String assetId, String entityType, String entityId) {
        var asset = assetRepository.findById(assetId)
                .orElseThrow(() -> new ResourceNotFoundException("Asset not found: " + assetId));

        AssetLink link = new AssetLink();
        link.setAsset(asset);
        link.setLinkedEntityType(entityType);
        link.setLinkedEntityId(entityId);
        return linkRepository.save(link);
    }

    @Override
    public void unlink(String linkId) {
        linkRepository.deleteById(linkId);
    }

    @Override
    public List<AssetLink> listByEntity(String entityType, String entityId) {
        return linkRepository.findByLinkedEntityTypeAndLinkedEntityId(entityType, entityId);
    }

    @Override
    public List<AssetLink> listByAsset(String assetId) {
        return linkRepository.findByAssetId(assetId);
    }
}
