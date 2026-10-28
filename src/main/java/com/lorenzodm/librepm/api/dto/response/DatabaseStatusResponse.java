package com.lorenzodm.librepm.api.dto.response;

/**
 * @param engine                database engine the backend runs on (sqlite, mariadb, h2, unknown)
 * @param fileTransferSupported true when raw .db export/import and encrypted backups are available
 *                              (local SQLite only; disabled on a shared MariaDB server)
 */
public record DatabaseStatusResponse(
        String dbPath,
        boolean exists,
        long sizeBytes,
        long lastModifiedEpochMs,
        boolean sqlite,
        String engine,
        boolean fileTransferSupported
) {
}
