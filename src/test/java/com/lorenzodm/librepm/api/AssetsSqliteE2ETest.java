package com.lorenzodm.librepm.api;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.lorenzodm.librepm.LibrePMApplication;
import com.lorenzodm.librepm.service.AssetService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.context.WebServerApplicationContext;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.jdbc.core.JdbcTemplate;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Single-PC (SQLite) mode: attachments are stored in the database too, and assets created
 * before that (bytes only on disk) keep working and get copied into the database.
 */
class AssetsSqliteE2ETest {
    private final HttpClient http = HttpClient.newHttpClient();
    private final ObjectMapper json = new ObjectMapper();

    @TempDir
    Path temporaryDirectory;

    @Test
    void attachmentsWorkLocallyAndLegacyOnDiskAssetsStayDownloadable() throws Exception {
        Path data = temporaryDirectory.resolve("pc");
        Files.createDirectories(data); // LibrePMApplication.main creates it in the real app
        try (ConfigurableApplicationContext pc = new SpringApplicationBuilder(LibrePMApplication.class)
                .properties("spring.task.scheduling.enabled=false")
                .run("--server.port=0", "--librepm.data.path=" + data,
                        "--librepm.assets.backfill-on-startup=false",
                        "--logging.file.name=" + data.resolve("backend.log"))) {
            String a = "http://127.0.0.1:" + ((WebServerApplicationContext) pc).getWebServer().getPort() + "/api";
            JdbcTemplate sql = pc.getBean(JdbcTemplate.class);
            Path assets = data.resolve("assets");

            String alice = post(a, "/users", null, Map.of("username", "alice", "displayName", "Alice", "password", "password123"), 201).get("id").asText();
            String bob = post(a, "/users", null, Map.of("username", "bob", "displayName", "Bob", "password", "password123"), 201).get("id").asText();
            String carol = post(a, "/users", null, Map.of("username", "carol", "displayName", "Carol", "password", "password123"), 201).get("id").asText();
            String project = post(a, "/users/" + alice + "/projects", alice, Map.of("name", "Local files"), 201).get("id").asText();
            String task = post(a, "/users/" + alice + "/projects/" + project + "/tasks", alice, Map.of("title", "Local task"), 201).get("id").asText();
            post(a, "/users/" + alice + "/projects/" + project + "/members", alice, Map.of("userId", bob, "role", "VIEWER"), 200);

            // New uploads land both in the local folder and in the database.
            byte[] binary = new byte[1_300_000];
            new Random(3).nextBytes(binary);
            JsonNode uploaded = upload(a, "/users/" + alice + "/assets/upload?taskId=" + task, alice, "scan.pdf", binary, 201);
            String uploadedId = uploaded.get("id").asText();
            assertEquals(project, uploaded.get("projectId").asText());
            assertEquals(3, count(sql, uploadedId));
            assertArrayEquals(binary, download(a, "/users/" + alice + "/assets/" + uploadedId + "/download", alice, 200));
            // Viewers can read but not upload; strangers can do neither.
            assertArrayEquals(binary, download(a, "/assets/" + uploadedId + "/download", bob, 200));
            upload(a, "/tasks/" + task + "/assets", bob, "viewer.txt", "x".getBytes(), 403);
            download(a, "/assets/" + uploadedId + "/download", carol, 403);
            get(a, "/tasks/" + task + "/assets", carol, 403);
            // Losing the local file is not a problem anymore: the database restores the cache.
            Files.delete(assets.resolve(uploaded.get("filePath").asText()));
            assertArrayEquals(binary, download(a, "/assets/" + uploadedId + "/download", bob, 200));
            assertTrue(Files.exists(assets.resolve(uploaded.get("filePath").asText())));

            // Legacy personal asset: metadata in the database, bytes only on disk.
            byte[] legacy = "legacy personal file".getBytes(StandardCharsets.UTF_8);
            Files.createDirectories(assets);
            Files.write(assets.resolve("legacy-personal.txt"), legacy);
            String legacyId = post(a, "/users/" + alice + "/assets/metadata", alice, Map.of(
                    "fileName", "legacy.txt", "filePath", "legacy-personal.txt", "mimeType", "text/plain", "sizeBytes", legacy.length), 201).get("id").asText();
            assertEquals(0, count(sql, legacyId));
            assertArrayEquals(legacy, download(a, "/users/" + alice + "/assets/" + legacyId + "/download", alice, 200));
            assertEquals(1, count(sql, legacyId), "Downloading a legacy asset copies it into the database");
            download(a, "/users/" + bob + "/assets/" + legacyId + "/download", bob, 403);

            // Legacy task asset written before V64 (no project_id): backfilled at startup, readable by members.
            byte[] legacyTask = "legacy task attachment".getBytes(StandardCharsets.UTF_8);
            Files.write(assets.resolve("legacy-task.txt"), legacyTask);
            String legacyTaskId = UUID.randomUUID().toString();
            sql.update("INSERT INTO assets (id, file_name, file_path, mime_type, size_bytes, deleted, created_at, updated_at, owner_id, task_id) " +
                    "VALUES (?, 'old.txt', 'legacy-task.txt', 'text/plain', ?, 0, '2024-01-01 10:00:00', '2024-01-01 10:00:00', ?, ?)",
                    legacyTaskId, legacyTask.length, alice, task);
            assertTrue(pc.getBean(AssetService.class).backfillLocalContents() >= 1);
            assertEquals(1, count(sql, legacyTaskId));
            Files.delete(assets.resolve("legacy-task.txt"));
            assertArrayEquals(legacyTask, download(a, "/assets/" + legacyTaskId + "/download", bob, 200));
            download(a, "/assets/" + legacyTaskId + "/download", carol, 403);
            assertTrue(ids(get(a, "/tasks/" + task + "/assets", bob, 200)).contains(legacyTaskId));

            // Deleting removes the file everywhere.
            delete(a, "/assets/" + uploadedId, bob, 403);
            delete(a, "/assets/" + uploadedId, alice, 204);
            assertEquals(0, count(sql, uploadedId));
            download(a, "/assets/" + uploadedId + "/download", alice, 404);
            assertFalse(ids(get(a, "/tasks/" + task + "/assets", alice, 200)).contains(uploadedId));
        }
    }

    private static int count(JdbcTemplate sql, String assetId) {
        return Objects.requireNonNull(sql.queryForObject("SELECT COUNT(*) FROM asset_contents WHERE asset_id = ?", Integer.class, assetId));
    }

    private static List<String> ids(JsonNode array) {
        List<String> ids = new ArrayList<>();
        array.forEach(node -> ids.add(node.get("id").asText()));
        return ids;
    }

    private JsonNode get(String base, String path, String user, int status) throws Exception {
        return send(HttpRequest.newBuilder(URI.create(base + path)).GET(), user, status);
    }

    private JsonNode post(String base, String path, String user, Object body, int status) throws Exception {
        return send(HttpRequest.newBuilder(URI.create(base + path)).POST(HttpRequest.BodyPublishers.ofString(json.writeValueAsString(body))), user, status);
    }

    private JsonNode delete(String base, String path, String user, int status) throws Exception {
        return send(HttpRequest.newBuilder(URI.create(base + path)).DELETE(), user, status);
    }

    private JsonNode send(HttpRequest.Builder request, String user, int status) throws Exception {
        request.header("Content-Type", "application/json");
        if (user != null) request.header("X-User-Id", user);
        HttpResponse<String> response = http.send(request.build(), HttpResponse.BodyHandlers.ofString());
        assertEquals(status, response.statusCode(), () -> request.build().method() + " " + request.build().uri() + " -> " + response.body());
        return response.body().isBlank() ? json.nullNode() : json.readTree(response.body());
    }

    private byte[] download(String base, String path, String user, int status) throws Exception {
        HttpRequest request = HttpRequest.newBuilder(URI.create(base + path)).header("X-User-Id", user).GET().build();
        HttpResponse<byte[]> response = http.send(request, HttpResponse.BodyHandlers.ofByteArray());
        assertEquals(status, response.statusCode(), () -> "GET " + path + " -> " + new String(response.body(), StandardCharsets.UTF_8));
        return response.body();
    }

    private JsonNode upload(String base, String path, String user, String fileName, byte[] content, int status) throws Exception {
        String boundary = "librepm-" + UUID.randomUUID();
        HttpRequest request = HttpRequest.newBuilder(URI.create(base + path))
                .header("X-User-Id", user)
                .header("Content-Type", "multipart/form-data; boundary=" + boundary)
                .POST(HttpRequest.BodyPublishers.ofByteArray(
                        CollaborativeAssetsMariaDbE2ETest.multipart(boundary, fileName, "application/octet-stream", content)))
                .build();
        HttpResponse<String> response = http.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(status, response.statusCode(), () -> "POST " + path + " -> " + response.body());
        return response.body().isBlank() ? json.nullNode() : json.readTree(response.body());
    }
}
