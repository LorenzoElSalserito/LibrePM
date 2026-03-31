package com.lorenzodm.librepm.api.dto.response;

public record DatabaseStatusResponse(
        String dbPath,
        boolean exists,
        long sizeBytes,
        long lastModifiedEpochMs,
        boolean sqlite
) {
}
