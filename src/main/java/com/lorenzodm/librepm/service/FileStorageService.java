package com.lorenzodm.librepm.service;

import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Path;

public interface FileStorageService {
    StoredFile store(MultipartFile file);
    Resource loadAsResource(String relativePath);

    /**
     * Resolves a stored relative path inside the local storage folder (the local cache
     * of attachment bytes), rejecting paths that escape it.
     */
    Path resolveLocal(String relativePath);

    /** Maximum accepted upload size in bytes, or a non-positive value for no limit. */
    long maxFileSize();

    record StoredFile(
            String relativePath,
            String originalFileName,
            String mimeType,
            long sizeBytes,
            String checksumSha256,
            Path absolutePath
    ) {}
}
