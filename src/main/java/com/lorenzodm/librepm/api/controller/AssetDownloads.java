package com.lorenzodm.librepm.api.controller;

import com.lorenzodm.librepm.core.entity.Asset;
import com.lorenzodm.librepm.service.AssetService;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.core.io.Resource;

import java.nio.charset.StandardCharsets;

/** Builds the HTTP response of an attachment download. */
final class AssetDownloads {
    private AssetDownloads() { }

    static ResponseEntity<Resource> toResponse(AssetService.AssetContent content) {
        Asset asset = content.asset();
        MediaType type;
        try {
            type = asset.getMimeType() == null || asset.getMimeType().isBlank()
                    ? MediaType.APPLICATION_OCTET_STREAM : MediaType.parseMediaType(asset.getMimeType());
        } catch (RuntimeException invalid) {
            type = MediaType.APPLICATION_OCTET_STREAM;
        }
        return ResponseEntity.ok()
                .contentType(type)
                .header(HttpHeaders.CONTENT_DISPOSITION, ContentDisposition.attachment()
                        .filename(asset.getFileName(), StandardCharsets.UTF_8).build().toString())
                .header("X-Content-Type-Options", "nosniff")
                .body(content.resource());
    }
}
