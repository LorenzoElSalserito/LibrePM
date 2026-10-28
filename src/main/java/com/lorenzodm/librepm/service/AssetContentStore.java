package com.lorenzodm.librepm.service;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Path;

/**
 * Stores attachment bytes in the database so that every backend connected to the same
 * (shared) database can serve a file, whichever PC uploaded it.
 */
public interface AssetContentStore {

    /** 512 KiB chunks stay below max_allowed_packet even on old MariaDB/MySQL defaults (1 MB). */
    int CHUNK_SIZE = 512 * 1024;

    /** @return true if the bytes of the asset are in the database. */
    boolean exists(String assetId);

    /** Replaces the stored bytes of the asset with the content of {@code file}. */
    void save(String assetId, Path file) throws IOException;

    /**
     * Writes the stored bytes to {@code out}, one chunk at a time.
     *
     * @return the number of bytes written
     */
    long copyTo(String assetId, OutputStream out) throws IOException;

    void delete(String assetId);

    /** Active assets whose bytes are not in the database yet, as id to stored relative path. */
    java.util.Map<String, String> missingContents();
}
