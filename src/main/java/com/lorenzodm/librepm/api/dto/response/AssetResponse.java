package com.lorenzodm.librepm.api.dto.response;

import java.time.Instant;

public record AssetResponse(
        String id,
        String fileName,
        String filePath,
        String mimeType,
        Long sizeBytes,
        String checksum,
        String description,
        String thumbnailPath,
        boolean deleted,
        Instant createdAt,
        Instant lastAccessedAt,
        Instant lastSyncedAt,
        String syncStatus,
        String cloudUrl,
        String ownerId
) {}
