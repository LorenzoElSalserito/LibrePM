package com.lorenzodm.librepm.service;

import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Path;

public interface FileStorageService {
    StoredFile store(MultipartFile file);
    Resource loadAsResource(String relativePath);

    record StoredFile(
            String relativePath,
            String originalFileName,
            String mimeType,
            long sizeBytes,
            String checksumSha256,
            Path absolutePath
    ) {}
}
