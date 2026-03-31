package com.lorenzodm.librepm.api.mapper;

import com.lorenzodm.librepm.api.dto.response.AssetResponse;
import com.lorenzodm.librepm.core.entity.Asset;
import org.springframework.stereotype.Component;

@Component
public class AssetMapper {

    public AssetResponse toResponse(Asset a) {
        return new AssetResponse(
                a.getId(),
                a.getFileName(),
                a.getFilePath(),
                a.getMimeType(),
                a.getSizeBytes(),
                a.getChecksum(),
                a.getDescription(),
                a.getThumbnailPath(),
                a.getDeletedAt() != null,
                a.getCreatedAt(),
                a.getLastAccessedAt(),
                a.getLastSyncedAt(),
                a.getSyncStatus() != null ? a.getSyncStatus().name() : null,
                a.getCloudUrl(),
                a.getOwner() != null ? a.getOwner().getId() : null
        );
    }
}
