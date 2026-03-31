package com.lorenzodm.librepm.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.lorenzodm.librepm.api.dto.response.DatabaseImportResponse;
import com.lorenzodm.librepm.api.dto.response.DatabaseStatusResponse;
import com.lorenzodm.librepm.config.LibrePMAssetsProperties;
import com.lorenzodm.librepm.config.LibrePMDataProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.InputStreamResource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.security.MessageDigest;
import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@Service
public class DatabaseTransferService {

    private static final Logger log = LoggerFactory.getLogger(DatabaseTransferService.class);

    private static final String SQLITE_HEADER = "SQLite format 3\u0000";

    private final LibrePMDataProperties dataProperties;
    private final LibrePMAssetsProperties assetsProperties;
    private final JdbcTemplate jdbcTemplate;
    private final ObjectMapper objectMapper;

    public DatabaseTransferService(
            LibrePMDataProperties dataProperties,
            LibrePMAssetsProperties assetsProperties,
            JdbcTemplate jdbcTemplate,
            ObjectMapper objectMapper
    ) {
        this.dataProperties = dataProperties;
        this.assetsProperties = assetsProperties;
        this.jdbcTemplate = jdbcTemplate;
        this.objectMapper = objectMapper;
    }

    public Path getDbPath() {
        return Paths.get(dataProperties.getPath()).resolve("librepm.db").toAbsolutePath().normalize();
    }

    public Path getConfigDir() {
        return Paths.get(dataProperties.getPath()).resolve("config").toAbsolutePath().normalize();
    }

    public Path getPendingImportDir() {
        return getConfigDir().resolve("db-import").toAbsolutePath().normalize();
    }

    public Path getPendingDbFile() {
        return getPendingImportDir().resolve("pending-librepm.db").toAbsolutePath().normalize();
    }

    public Path getPendingMarkerFile() {
        return getPendingImportDir().resolve("pending.marker").toAbsolutePath().normalize();
    }

    public DatabaseStatusResponse status() {
        Path db = getDbPath();
        boolean exists = Files.exists(db);
        long size = 0;
        long lastModified = 0;
        boolean sqlite = false;

        try {
            if (exists) {
                size = Files.size(db);
                lastModified = Files.getLastModifiedTime(db).toMillis();
                sqlite = isSqliteFile(db);
            }
        } catch (IOException e) {
            log.warn("Impossibile leggere status DB: {}", e.getMessage());
        }

        return new DatabaseStatusResponse(
                db.toString(),
                exists,
                size,
                lastModified,
                sqlite
        );
    }

    public ExportedFile exportDbSnapshot() {
        Path db = getDbPath();
        ensureExists(db);

        try {
            jdbcTemplate.execute("PRAGMA wal_checkpoint(FULL)");
        } catch (Exception e) {
            log.debug("PRAGMA wal_checkpoint(FULL) non eseguito (ok in alcuni setup): {}", e.getMessage());
        }

        Path exportsDir = getConfigDir().resolve("exports");
        mkdirs(exportsDir);

        String ts = DateTimeFormatter.ISO_INSTANT.format(Instant.now()).replace(":", "-");
        Path snapshot = exportsDir.resolve("librepm-export-" + ts + ".db");

        try {
            Files.copy(db, snapshot, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            throw new UncheckedIOException("Errore export snapshot DB", e);
        }

        return new ExportedFile(snapshot, "application/octet-stream", "LibrePM-db-" + ts + ".db");
    }

    public ExportedFile exportZip(boolean includeAssets) {
        Path db = getDbPath();
        ensureExists(db);

        try {
            jdbcTemplate.execute("PRAGMA wal_checkpoint(FULL)");
        } catch (Exception e) {
            log.debug("PRAGMA wal_checkpoint(FULL) non eseguito (ok): {}", e.getMessage());
        }

        Path exportsDir = getConfigDir().resolve("exports");
        mkdirs(exportsDir);

        String ts = DateTimeFormatter.ISO_INSTANT.format(Instant.now()).replace(":", "-");
        Path zipPath = exportsDir.resolve("librepm-export-" + ts + ".zip");

        Path assetsDir = Paths.get(assetsProperties.getStoragePath()).toAbsolutePath().normalize();

        try (ZipOutputStream zos = new ZipOutputStream(Files.newOutputStream(zipPath, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING))) {
            addFileToZip(zos, db, "db/librepm.db");

            Path wal = Paths.get(db.toString() + "-wal");
            Path shm = Paths.get(db.toString() + "-shm");
            if (Files.exists(wal)) addFileToZip(zos, wal, "db/librepm.db-wal");
            if (Files.exists(shm)) addFileToZip(zos, shm, "db/librepm.db-shm");

            if (includeAssets && Files.isDirectory(assetsDir)) {
                addDirectoryToZip(zos, assetsDir, "assets");
            }

            // Generate and include manifest
            String manifest = generateManifest(db);
            ZipEntry manifestEntry = new ZipEntry("manifest.json");
            zos.putNextEntry(manifestEntry);
            zos.write(manifest.getBytes(StandardCharsets.UTF_8));
            zos.closeEntry();
        } catch (IOException e) {
            throw new UncheckedIOException("Errore export ZIP", e);
        }

        return new ExportedFile(zipPath, "application/zip", "LibrePM-export-" + ts + ".zip");
    }

    public Map<String, Object> getBackupInfo() {
        Path db = getDbPath();
        try {
            ObjectNode node = objectMapper.readValue(generateManifest(db), ObjectNode.class);
            Map<String, Object> result = new LinkedHashMap<>();
            node.fields().forEachRemaining(entry -> result.put(entry.getKey(), entry.getValue()));
            return result;
        } catch (Exception e) {
            log.warn("Error generating backup info: {}", e.getMessage());
            return Map.of("error", e.getMessage());
        }
    }

    private String generateManifest(Path db) {
        try {
            ObjectNode manifest = objectMapper.createObjectNode();
            manifest.put("version", "1.0.0");
            manifest.put("appVersion", getAppVersion());
            manifest.put("createdAt", Instant.now().toString());

            long dbSize = Files.exists(db) ? Files.size(db) : 0;
            manifest.put("databaseSize", dbSize);

            if (Files.exists(db)) {
                manifest.put("checksumSHA256", computeSha256(db));
            }

            String migrationVersion = getMigrationVersion();
            manifest.put("migrationVersion", migrationVersion);

            ObjectNode counts = objectMapper.createObjectNode();
            counts.put("projects", countTable("projects"));
            counts.put("tasks", countTable("tasks"));
            counts.put("notes", countTable("notes"));
            counts.put("assets", countTable("assets"));
            counts.put("teams", countTable("teams"));
            counts.put("tags", countTable("tags"));
            manifest.set("entityCounts", counts);

            manifest.put("userCount", countTable("users"));
            manifest.put("exportType", "FULL_BACKUP");

            return objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(manifest);
        } catch (Exception e) {
            throw new RuntimeException("Error generating manifest", e);
        }
    }

    private long countTable(String tableName) {
        try {
            Long count = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM " + tableName, Long.class);
            return count != null ? count : 0;
        } catch (Exception e) {
            log.debug("Could not count table {}: {}", tableName, e.getMessage());
            return -1;
        }
    }

    private String getMigrationVersion() {
        try {
            return jdbcTemplate.queryForObject(
                    "SELECT version FROM flyway_schema_history ORDER BY installed_rank DESC LIMIT 1",
                    String.class);
        } catch (Exception e) {
            return "unknown";
        }
    }

    private String getAppVersion() {
        try {
            var props = new Properties();
            var is = getClass().getResourceAsStream("/application.properties");
            if (is != null) {
                props.load(is);
                return props.getProperty("app.version", "unknown");
            }
        } catch (Exception ignored) {}
        return "unknown";
    }

    private String computeSha256(Path file) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] data = Files.readAllBytes(file);
            byte[] hash = md.digest(data);
            StringBuilder sb = new StringBuilder();
            for (byte b : hash) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (Exception e) {
            return "error";
        }
    }

    public DatabaseImportResponse importDb(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            return new DatabaseImportResponse(false, false, null, "File mancante o vuoto");
        }

        String original = file.getOriginalFilename() == null ? "" : file.getOriginalFilename().toLowerCase();
        if (!original.endsWith(".db")) {
            return new DatabaseImportResponse(false, false, null, "Il file deve avere estensione .db (SQLite)");
        }

        mkdirs(getPendingImportDir());

        Path pending = getPendingDbFile();
        Path marker = getPendingMarkerFile();

        try {
            try (InputStream in = new BufferedInputStream(file.getInputStream())) {
                Files.copy(in, pending, StandardCopyOption.REPLACE_EXISTING);
            }

            if (!isSqliteFile(pending)) {
                Files.deleteIfExists(pending);
                Files.deleteIfExists(marker);
                return new DatabaseImportResponse(false, false, null, "Il file non sembra un database SQLite valido");
            }

            Files.writeString(marker, "PENDING", StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);

            return new DatabaseImportResponse(
                    true,
                    true,
                    pending.toString(),
                    "Import accettato: verrà applicato al prossimo avvio del backend LibrePM (Electron potrà riavviare il processo)."
            );

        } catch (IOException e) {
            return new DatabaseImportResponse(false, false, null, "Errore import: " + e.getMessage());
        }
    }

    public DatabaseImportResponse importDecryptedDb(byte[] data) {
        if (data == null || data.length < SQLITE_HEADER.length()) {
            return new DatabaseImportResponse(false, false, null, "Decrypted data is empty or too small");
        }

        String header = new String(data, 0, SQLITE_HEADER.length());
        if (!SQLITE_HEADER.equals(header)) {
            return new DatabaseImportResponse(false, false, null, "Decrypted data is not a valid SQLite database");
        }

        mkdirs(getPendingImportDir());
        Path pending = getPendingDbFile();
        Path marker = getPendingMarkerFile();

        try {
            Files.write(pending, data, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
            Files.writeString(marker, "PENDING", StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
            return new DatabaseImportResponse(true, true, pending.toString(),
                    "Encrypted import accepted: will be applied on next backend restart.");
        } catch (IOException e) {
            return new DatabaseImportResponse(false, false, null, "Error writing decrypted DB: " + e.getMessage());
        }
    }

    private boolean isSqliteFile(Path path) {
        if (!Files.exists(path)) return false;
        try (InputStream is = Files.newInputStream(path, StandardOpenOption.READ)) {
            byte[] header = new byte[SQLITE_HEADER.length()];
            int read = is.read(header);
            if (read != SQLITE_HEADER.length()) return false;
            String s = new String(header);
            return SQLITE_HEADER.equals(s);
        } catch (IOException e) {
            return false;
        }
    }

    private void addFileToZip(ZipOutputStream zos, Path file, String entryName) throws IOException {
        ZipEntry entry = new ZipEntry(entryName);
        entry.setTime(Files.getLastModifiedTime(file).toMillis());
        zos.putNextEntry(entry);
        Files.copy(file, zos);
        zos.closeEntry();
    }

    private void addDirectoryToZip(ZipOutputStream zos, Path dir, String prefix) throws IOException {
        Files.walk(dir)
                .sorted(Comparator.comparing(Path::toString))
                .forEach(p -> {
                    try {
                        if (Files.isDirectory(p)) return;
                        Path rel = dir.relativize(p);
                        String entryName = prefix + "/" + rel.toString().replace('\\', '/');
                        addFileToZip(zos, p, entryName);
                    } catch (IOException e) {
                        throw new UncheckedIOException(e);
                    }
                });
    }

    private void ensureExists(Path p) {
        if (!Files.exists(p)) {
            throw new IllegalStateException("Database non trovato: " + p);
        }
    }

    private void mkdirs(Path p) {
        try {
            Files.createDirectories(p);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public record ExportedFile(Path path, String contentType, String downloadName) {
    }

    public InputStreamResource asResource(Path path) {
        try {
            return new InputStreamResource(Files.newInputStream(path, StandardOpenOption.READ));
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
