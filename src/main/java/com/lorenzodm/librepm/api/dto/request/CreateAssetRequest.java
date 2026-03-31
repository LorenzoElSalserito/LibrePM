package com.lorenzodm.librepm.api.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CreateAssetRequest(
        @NotBlank @Size(max = 500) String fileName,
        @NotBlank @Size(max = 1000) String filePath,
        @Size(max = 100) String mimeType,
        @NotNull Long sizeBytes,
        @Size(max = 64) String checksum,
        @Size(max = 500) String description,
        @Size(max = 1000) String thumbnailPath
) {}
