package com.lorenzodm.librepm.service.impl;

import com.lorenzodm.librepm.service.AssetContentStore;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * {@link AssetContentStore} on the {@code asset_contents} table. Plain JDBC keeps BLOB
 * handling identical on SQLite (no java.sql.Blob support) and MariaDB (LONGBLOB), and
 * lets reads stream chunk by chunk instead of loading whole files in memory.
 * Writes join the caller's transaction.
 */
@Service
public class JdbcAssetContentStore implements AssetContentStore {

    private final JdbcTemplate jdbc;

    public JdbcAssetContentStore(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    public boolean exists(String assetId) {
        Integer count = jdbc.queryForObject(
                "SELECT COUNT(*) FROM asset_contents WHERE asset_id = ? AND chunk_index = 0", Integer.class, assetId);
        return count != null && count > 0;
    }

    @Override
    public void save(String assetId, Path file) throws IOException {
        jdbc.update("DELETE FROM asset_contents WHERE asset_id = ?", assetId);
        try (InputStream in = Files.newInputStream(file)) {
            int index = 0;
            byte[] chunk;
            do {
                chunk = in.readNBytes(CHUNK_SIZE);
                if (chunk.length == 0 && index > 0) break;
                // An empty file is still stored as one empty chunk, so exists() holds.
                jdbc.update("INSERT INTO asset_contents (asset_id, chunk_index, chunk_size, data) VALUES (?, ?, ?, ?)",
                        assetId, index++, chunk.length, chunk);
            } while (chunk.length == CHUNK_SIZE);
        }
    }

    @Override
    public long copyTo(String assetId, OutputStream out) throws IOException {
        List<Integer> chunks = jdbc.queryForList(
                "SELECT chunk_index FROM asset_contents WHERE asset_id = ? ORDER BY chunk_index", Integer.class, assetId);
        long written = 0;
        for (int expected = 0; expected < chunks.size(); expected++) {
            if (chunks.get(expected) != expected) {
                throw new IOException("Contenuto allegato incompleto nel database: " + assetId);
            }
            byte[] data = jdbc.queryForObject(
                    "SELECT data FROM asset_contents WHERE asset_id = ? AND chunk_index = ?",
                    (rs, row) -> rs.getBytes(1), assetId, expected);
            if (data != null) {
                out.write(data);
                written += data.length;
            }
        }
        return written;
    }

    @Override
    public Map<String, String> missingContents() {
        Map<String, String> missing = new LinkedHashMap<>();
        jdbc.query("SELECT a.id, a.file_path FROM assets a WHERE a.deleted_at IS NULL AND NOT EXISTS " +
                        "(SELECT 1 FROM asset_contents c WHERE c.asset_id = a.id AND c.chunk_index = 0)",
                rs -> { missing.put(rs.getString(1), rs.getString(2)); });
        return missing;
    }

    @Override
    public void delete(String assetId) {
        jdbc.update("DELETE FROM asset_contents WHERE asset_id = ?", assetId);
    }
}
