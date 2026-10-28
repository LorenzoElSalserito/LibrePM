package com.lorenzodm.librepm.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.lorenzodm.librepm.api.dto.response.DatabaseImportResponse;
import com.lorenzodm.librepm.api.dto.response.DatabaseStatusResponse;
import com.lorenzodm.librepm.config.LibrePMAssetsProperties;
import com.lorenzodm.librepm.config.LibrePMDataProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
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
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
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
    private final String datasourceUrl;

    /** Tables every LibrePM SQLite database must contain to be accepted for import. */
    static final List<String> REQUIRED_TABLES = List.of("flyway_schema_history", "users", "projects", "tasks");

    private static final Pattern MIGRATION_VERSION = Pattern.compile("^V(\\d+)__.*\\.sql$");

    public DatabaseTransferService(
            LibrePMDataProperties dataProperties,
            LibrePMAssetsProperties assetsProperties,
            JdbcTemplate jdbcTemplate,
            ObjectMapper objectMapper,
            @Value("${spring.datasource.url:}") String datasourceUrl
    ) {
        this.dataProperties = dataProperties;
        this.assetsProperties = assetsProperties;
        this.jdbcTemplate = jdbcTemplate;
        this.objectMapper = objectMapper;
        this.datasourceUrl = datasourceUrl == null ? "" : datasourceUrl;
    }

    /**
     * Raw database file transfer (.db export/import, encrypted backups) only makes sense when the
     * backend runs on its local SQLite file. On a shared MariaDB server these operations are refused.
     */
    public boolean isFileTransferSupported() {
        return datasourceUrl.startsWith("jdbc:sqlite:");
    }

    public String getEngine() {
        if (datasourceUrl.startsWith("jdbc:sqlite:")) return "sqlite";
        if (datasourceUrl.startsWith("jdbc:mariadb:") || datasourceUrl.startsWith("jdbc:mysql:")) return "mariadb";
        if (datasourceUrl.startsWith("jdbc:h2:")) return "h2";
        return "unknown";
    }

    private void requireFileTransferSupported() {
        if (!isFileTransferSupported()) {
            throw new FileTransferUnsupportedException(
                    "Database file export/import is only available with the local SQLite database. "
                            + "This backend uses a shared " + getEngine() + " database: use the JSON export/import instead.");
        }
    }

    /** Thrown when a raw database file operation is requested on a non-SQLite backend. */
    public static class FileTransferUnsupportedException extends RuntimeException {
        public FileTransferUnsupportedException(String message) {
            super(message);
        }
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
                isFileTransferSupported() ? db.toString() : null,
                exists,
                size,
                lastModified,
                sqlite,
                getEngine(),
                isFileTransferSupported()
        );
    }

    public ExportedFile exportDbSnapshot() {
        requireFileTransferSupported();
        Path db = getDbPath();
        ensureExists(db);

        String ts = DateTimeFormatter.ISO_INSTANT.format(Instant.now()).replace(":", "-");
        Path exportsDir = getConfigDir().resolve("exports");
        mkdirs(exportsDir);
        pruneOldExports(exportsDir);
        Path snapshot = exportsDir.resolve("librepm-export-" + ts + ".db");

        // VACUUM INTO writes a consistent, self-contained copy (WAL content included) without
        // racing concurrent writers. Fall back to checkpoint + file copy on older SQLite builds.
        try {
            Files.deleteIfExists(snapshot);
            jdbcTemplate.execute("VACUUM INTO '" + snapshot.toString().replace("'", "''") + "'");
            if (isSqliteFile(snapshot)) {
                return new ExportedFile(snapshot, "application/octet-stream", "LibrePM-db-" + ts + ".db");
            }
        } catch (Exception e) {
            log.debug("VACUUM INTO non disponibile, uso checkpoint + copia: {}", e.getMessage());
        }

        try {
            jdbcTemplate.execute("PRAGMA wal_checkpoint(FULL)");
        } catch (Exception e) {
            log.debug("PRAGMA wal_checkpoint(FULL) non eseguito (ok in alcuni setup): {}", e.getMessage());
        }
        return copySnapshot(db, snapshot, ts);
    }

    /** Snapshots are only staging copies for the HTTP download: keep the most recent few. */
    private void pruneOldExports(Path exportsDir) {
        try (var files = Files.list(exportsDir)) {
            List<Path> old = files
                    .filter(p -> p.getFileName().toString().startsWith("librepm-export-"))
                    .sorted(Comparator.comparing(Path::toString).reversed())
                    .skip(4)
                    .toList();
            old.forEach(DatabaseTransferService::deleteQuietly);
        } catch (IOException e) {
            log.debug("Pulizia export precedenti non riuscita: {}", e.getMessage());
        }
    }

    private ExportedFile copySnapshot(Path db, Path snapshot, String ts) {
        try {
            Files.copy(db, snapshot, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            throw new UncheckedIOException("Errore export snapshot DB", e);
        }

        return new ExportedFile(snapshot, "application/octet-stream", "LibrePM-db-" + ts + ".db");
    }

    public ExportedFile exportZip(boolean includeAssets) {
        requireFileTransferSupported();
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
        try (InputStream in = new BufferedInputStream(file.getInputStream())) {
            return importDbStream(in);
        } catch (IOException e) {
            return new DatabaseImportResponse(false, false, null, "Errore import: " + e.getMessage());
        }
    }

    /**
     * Stages a LibrePM SQLite database for import. The file is validated (SQLite header, integrity,
     * LibrePM schema, not newer than this app) and written to the pending-import slot; it replaces
     * the live database on the next backend start (see PendingDbImportRunListener), which first
     * backs up the current database. The caller (Electron) restarts the backend right after.
     */
    public DatabaseImportResponse importDbStream(InputStream in) {
        requireFileTransferSupported();
        mkdirs(getPendingImportDir());
        Path partial = getPendingImportDir().resolve("incoming-" + UUID.randomUUID() + ".db");
        try {
            Files.copy(in, partial, StandardCopyOption.REPLACE_EXISTING);
            return stageValidatedFile(partial);
        } catch (IOException e) {
            return new DatabaseImportResponse(false, false, null, "Errore import: " + e.getMessage());
        } finally {
            deleteQuietly(partial);
        }
    }

    public DatabaseImportResponse importDecryptedDb(byte[] data) {
        if (data == null || data.length < SQLITE_HEADER.length()) {
            return new DatabaseImportResponse(false, false, null, "Decrypted data is empty or too small");
        }
        return importDbStream(new java.io.ByteArrayInputStream(data));
    }

    private DatabaseImportResponse stageValidatedFile(Path candidate) throws IOException {
        String problem = validateLibrePmDatabase(candidate);
        if (problem != null) {
            return new DatabaseImportResponse(false, false, null, problem);
        }

        Path pending = getPendingDbFile();
        Path marker = getPendingMarkerFile();
        Files.copy(candidate, pending, StandardCopyOption.REPLACE_EXISTING);
        Files.writeString(marker, "PENDING", StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);

        // Flush the WAL of the live database so the pre-import backup taken at restart is complete.
        try {
            jdbcTemplate.execute("PRAGMA wal_checkpoint(TRUNCATE)");
        } catch (Exception e) {
            log.debug("PRAGMA wal_checkpoint(TRUNCATE) non eseguito: {}", e.getMessage());
        }

        return new DatabaseImportResponse(
                true,
                true,
                pending.toString(),
                "Import accepted: the database will be replaced when the backend restarts (a backup of the current database is kept)."
        );
    }

    /**
     * @return null when the file is a usable LibrePM database, otherwise a human readable reason.
     */
    public String validateLibrePmDatabase(Path file) {
        if (!isSqliteFile(file)) {
            return "The file is not a SQLite database";
        }
        Path probe = null;
        try {
            // Work on a throw-away copy: opening a database may create journal files next to it.
            probe = Files.createTempFile(file.getParent(), "probe-", ".db");
            Files.copy(file, probe, StandardCopyOption.REPLACE_EXISTING);
            try (Connection c = DriverManager.getConnection("jdbc:sqlite:" + probe.toAbsolutePath());
                 Statement st = c.createStatement()) {
                try (ResultSet rs = st.executeQuery("PRAGMA quick_check")) {
                    String result = rs.next() ? rs.getString(1) : null;
                    if (!"ok".equalsIgnoreCase(result)) {
                        return "The database is damaged (integrity check: " + result + ")";
                    }
                }
                Set<String> tables = new HashSet<>();
                try (ResultSet rs = st.executeQuery("SELECT name FROM sqlite_master WHERE type='table'")) {
                    while (rs.next()) tables.add(rs.getString(1).toLowerCase(Locale.ROOT));
                }
                for (String required : REQUIRED_TABLES) {
                    if (!tables.contains(required)) {
                        return "The file is not a LibrePM database (missing table '" + required + "')";
                    }
                }
                int imported = 0;
                try (ResultSet rs = st.executeQuery(
                        "SELECT version FROM flyway_schema_history WHERE success = 1 AND version IS NOT NULL")) {
                    while (rs.next()) {
                        try {
                            imported = Math.max(imported, Integer.parseInt(rs.getString(1).trim()));
                        } catch (NumberFormatException ignored) {
                            // non-numeric versions are not produced by LibrePM
                        }
                    }
                }
                int supported = latestBundledMigration();
                if (supported > 0 && imported > supported) {
                    return "The database was created by a newer LibrePM version (schema " + imported
                            + ", this app supports up to " + supported + "). Update LibrePM first.";
                }
            }
            return null;
        } catch (Exception e) {
            return "The file cannot be read as a LibrePM database: " + e.getMessage();
        } finally {
            if (probe != null) {
                deleteQuietly(probe);
                deleteQuietly(Paths.get(probe + "-wal"));
                deleteQuietly(Paths.get(probe + "-shm"));
                deleteQuietly(Paths.get(probe + "-journal"));
            }
        }
    }

    static int latestBundledMigration() {
        try {
            Resource[] resources = new PathMatchingResourcePatternResolver()
                    .getResources("classpath*:db/migration/V*__*.sql");
            int max = 0;
            for (Resource r : resources) {
                String name = r.getFilename();
                if (name == null) continue;
                Matcher m = MIGRATION_VERSION.matcher(name);
                if (m.matches()) max = Math.max(max, Integer.parseInt(m.group(1)));
            }
            return max;
        } catch (IOException e) {
            return 0;
        }
    }

    private static void deleteQuietly(Path p) {
        try {
            Files.deleteIfExists(p);
        } catch (IOException ignored) {
            // best effort
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
