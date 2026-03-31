package com.lorenzodm.librepm.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.lorenzodm.librepm.config.LibrePMDataProperties;
import com.lorenzodm.librepm.core.entity.EventJournalEntry;
import com.lorenzodm.librepm.service.DiagnosticsService;
import com.lorenzodm.librepm.service.EventJournalService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@Service
public class DiagnosticsServiceImpl implements DiagnosticsService {

    private static final Logger log = LoggerFactory.getLogger(DiagnosticsServiceImpl.class);

    private final ObjectMapper objectMapper;
    private final JdbcTemplate jdbcTemplate;
    private final LibrePMDataProperties dataProperties;
    private final EventJournalService eventJournalService;

    public DiagnosticsServiceImpl(ObjectMapper objectMapper,
                                  JdbcTemplate jdbcTemplate,
                                  LibrePMDataProperties dataProperties,
                                  EventJournalService eventJournalService) {
        this.objectMapper = objectMapper;
        this.jdbcTemplate = jdbcTemplate;
        this.dataProperties = dataProperties;
        this.eventJournalService = eventJournalService;
    }

    @Override
    public Path generateBundle() {
        try {
            Path tempDir = Files.createTempDirectory("librepm-diag-");
            Path zipPath = tempDir.resolve("librepm-diagnostics-" +
                    Instant.now().toString().replace(":", "-") + ".zip");

            try (ZipOutputStream zos = new ZipOutputStream(
                    Files.newOutputStream(zipPath, StandardOpenOption.CREATE))) {

                addEntry(zos, "system-info.json", generateSystemInfo());
                addEntry(zos, "health-report.json", generateHealthReport());
                addEntry(zos, "recent-events.json", generateRecentEvents());
                addEntry(zos, "migration-history.json", generateMigrationHistory());
            }

            return zipPath;
        } catch (IOException e) {
            throw new UncheckedIOException("Failed to generate diagnostics bundle", e);
        }
    }

    private String generateSystemInfo() throws IOException {
        ObjectNode info = objectMapper.createObjectNode();
        info.put("generatedAt", Instant.now().toString());
        info.put("javaVersion", System.getProperty("java.version"));
        info.put("javaVendor", System.getProperty("java.vendor"));
        info.put("osName", System.getProperty("os.name"));
        info.put("osVersion", System.getProperty("os.version"));
        info.put("osArch", System.getProperty("os.arch"));

        // Database size
        Path dbPath = Paths.get(dataProperties.getPath()).resolve("librepm.db");
        if (Files.exists(dbPath)) {
            info.put("databaseSizeBytes", Files.size(dbPath));
        }

        // Entity counts
        ObjectNode counts = objectMapper.createObjectNode();
        for (String table : List.of("projects", "tasks", "notes", "assets", "users", "teams", "tags")) {
            try {
                Long count = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM " + table, Long.class);
                counts.put(table, count != null ? count : 0);
            } catch (Exception e) {
                counts.put(table, -1);
            }
        }
        info.set("entityCounts", counts);

        // Disk space
        Path dataDir = Paths.get(dataProperties.getPath());
        if (Files.exists(dataDir)) {
            var store = Files.getFileStore(dataDir);
            info.put("diskTotalBytes", store.getTotalSpace());
            info.put("diskFreeBytes", store.getUsableSpace());
        }

        // Memory
        Runtime rt = Runtime.getRuntime();
        info.put("jvmMaxMemoryMB", rt.maxMemory() / (1024 * 1024));
        info.put("jvmTotalMemoryMB", rt.totalMemory() / (1024 * 1024));
        info.put("jvmFreeMemoryMB", rt.freeMemory() / (1024 * 1024));

        return objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(info);
    }

    private String generateHealthReport() throws IOException {
        ObjectNode health = objectMapper.createObjectNode();
        health.put("status", "UP");
        health.put("checkedAt", Instant.now().toString());

        // Check database connectivity
        try {
            jdbcTemplate.queryForObject("SELECT 1", Integer.class);
            health.put("database", "OK");
        } catch (Exception e) {
            health.put("database", "ERROR: " + e.getMessage());
        }

        // Check Flyway
        try {
            String version = jdbcTemplate.queryForObject(
                    "SELECT version FROM flyway_schema_history ORDER BY installed_rank DESC LIMIT 1",
                    String.class);
            health.put("migrationVersion", version);
        } catch (Exception e) {
            health.put("migrationVersion", "unknown");
        }

        return objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(health);
    }

    private String generateRecentEvents() throws IOException {
        List<EventJournalEntry> entries = eventJournalService.recent(100);
        ArrayNode arr = objectMapper.createArrayNode();
        for (EventJournalEntry entry : entries) {
            ObjectNode node = objectMapper.createObjectNode();
            node.put("id", entry.getId());
            node.put("eventType", entry.getEventType());
            node.put("entityType", entry.getEntityType() != null ? entry.getEntityType() : "");
            // Anonymize: don't include entityId or userId
            node.put("timestamp", entry.getTimestamp().toString());
            arr.add(node);
        }
        return objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(arr);
    }

    private String generateMigrationHistory() throws IOException {
        ArrayNode arr = objectMapper.createArrayNode();
        try {
            List<Map<String, Object>> rows = jdbcTemplate.queryForList(
                    "SELECT installed_rank, version, description, type, script, installed_on, success " +
                    "FROM flyway_schema_history ORDER BY installed_rank");
            for (Map<String, Object> row : rows) {
                ObjectNode node = objectMapper.createObjectNode();
                node.put("rank", (Integer) row.get("installed_rank"));
                node.put("version", String.valueOf(row.get("version")));
                node.put("description", String.valueOf(row.get("description")));
                node.put("type", String.valueOf(row.get("type")));
                node.put("script", String.valueOf(row.get("script")));
                node.put("installedOn", String.valueOf(row.get("installed_on")));
                node.put("success", Boolean.TRUE.equals(row.get("success")));
                arr.add(node);
            }
        } catch (Exception e) {
            log.warn("Could not read migration history: {}", e.getMessage());
        }
        return objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(arr);
    }

    private void addEntry(ZipOutputStream zos, String name, String content) throws IOException {
        zos.putNextEntry(new ZipEntry(name));
        zos.write(content.getBytes(StandardCharsets.UTF_8));
        zos.closeEntry();
    }
}
