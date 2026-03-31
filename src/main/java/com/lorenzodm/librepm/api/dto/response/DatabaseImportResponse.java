package com.lorenzodm.librepm.api.dto.response;

public record DatabaseImportResponse(
        boolean accepted,
        boolean appliedOnNextStart,
        String pendingFilePath,
        String message
) {
}
