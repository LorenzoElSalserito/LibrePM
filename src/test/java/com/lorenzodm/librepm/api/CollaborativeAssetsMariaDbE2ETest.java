package com.lorenzodm.librepm.api;

import ch.vorburger.mariadb4j.DB;
import ch.vorburger.mariadb4j.DBConfigurationBuilder;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.lorenzodm.librepm.LibrePMApplication;
import com.lorenzodm.librepm.migration.SqliteToMariaDbMigrator;
import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.context.WebServerApplicationContext;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

import java.io.ByteArrayOutputStream;
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
 * Two desktop backends share one real MariaDB: a file attached on one PC must be listed,
 * downloaded byte-identical and notified on the other, and stay closed to non-members.
 */
class CollaborativeAssetsMariaDbE2ETest {
    private static final long MAX_FILE_SIZE = 8L * 1024 * 1024;

    private final HttpClient http = HttpClient.newHttpClient();
    private final ObjectMapper json = new ObjectMapper();

    @TempDir
    Path temporaryDirectory;

    @Test
    void attachmentsTravelBetweenPcsThroughTheSharedDatabase() throws Exception {
        Path sqlite = temporaryDirectory.resolve("schema.db");
        Flyway.configure().dataSource("jdbc:sqlite:" + sqlite, null, null).locations("classpath:db/migration").load().migrate();

        DBConfigurationBuilder configuration = DBConfigurationBuilder.newBuilder();
        configuration.setPort(0);
        configuration.setBaseDir(temporaryDirectory.resolve("mariadb-base").toFile());
        configuration.setDataDir(temporaryDirectory.resolve("mariadb-data").toFile());
        DB database = DB.newEmbeddedDB(configuration.build());
        database.start();
        try {
            String url = configuration.getURL("librepm_assets_e2e") + "?createDatabaseIfNotExist=true";
            DriverManagerDataSource dataSource = new DriverManagerDataSource(url, "root", "");
            new SqliteToMariaDbMigrator().migrate(sqlite, dataSource, false);
            JdbcTemplate sql = new JdbcTemplate(dataSource);
            assertEquals("longblob", sql.queryForObject(
                    "SELECT data_type FROM information_schema.columns WHERE table_schema = DATABASE() " +
                    "AND table_name = 'asset_contents' AND column_name = 'data'", String.class));

            try (ConfigurableApplicationContext alicePc = start(url, "alice");
                 ConfigurableApplicationContext bobPc = start(url, "bob")) {
                String a = base(alicePc);
                String b = base(bobPc);
                long stamp = System.nanoTime();

                String alice = post(a, "/users", null, Map.of("username", "alice" + stamp, "displayName", "Alice", "password", "password123"), 201).get("id").asText();
                String bob = post(a, "/users", null, Map.of("username", "bob" + stamp, "displayName", "Bob", "password", "password123"), 201).get("id").asText();
                String carol = post(b, "/users", null, Map.of("username", "carol" + stamp, "displayName", "Carol", "password", "password123"), 201).get("id").asText();

                String project = post(a, "/users/" + alice + "/projects", alice, Map.of("name", "Shared files"), 201).get("id").asText();
                String task = post(a, "/users/" + alice + "/projects/" + project + "/tasks", alice, Map.of("title", "Budget review"), 201).get("id").asText();
                post(a, "/users/" + alice + "/projects/" + project + "/members", alice, Map.of("userId", bob, "role", "EDITOR"), 200);

                // Alice attaches files on her PC: a text file on the task, a few MB of binary on the project,
                // and an image through the legacy endpoint the task editor uses.
                byte[] text = "Budget 2026\nCaffè € ✓\n".getBytes(StandardCharsets.UTF_8);
                byte[] binary = new byte[3 * 1024 * 1024 + 12345];
                new Random(42).nextBytes(binary);
                byte[] image = new byte[70_000];
                new Random(7).nextBytes(image);
                String textId = upload(a, "/tasks/" + task + "/assets", alice, "budget notes.txt", "text/plain", text, 201).get("id").asText();
                JsonNode binaryAsset = upload(a, "/projects/" + project + "/assets", alice, "drawings.pdf", "application/pdf", binary, 201);
                String binaryId = binaryAsset.get("id").asText();
                assertEquals(project, binaryAsset.get("projectId").asText());
                assertEquals("Alice", binaryAsset.get("ownerName").asText());
                String imageId = upload(a, "/users/" + alice + "/assets/upload?taskId=" + task, alice, "photo.png", "image/png", image, 201).get("id").asText();
                assertEquals(7, sql.queryForObject("SELECT COUNT(*) FROM asset_contents WHERE asset_id = ?", Integer.class, binaryId),
                        "Large files are stored in 512 KiB chunks");

                // Bob lists and downloads them on his own PC, byte-identical.
                assertEquals(Set.of(textId, binaryId, imageId), new HashSet<>(ids(get(b, "/projects/" + project + "/assets", bob, 200))));
                assertEquals(Set.of(textId, imageId), new HashSet<>(ids(get(b, "/tasks/" + task + "/assets", bob, 200))));
                Path bobCache = temporaryDirectory.resolve("client-bob").resolve("assets");
                assertFalse(Files.exists(bobCache) && Files.list(bobCache).findAny().isPresent(), "Nothing is on Bob's disk before downloading");
                HttpResponse<byte[]> textDownload = download(b, "/assets/" + textId + "/download", bob, 200);
                assertArrayEquals(text, textDownload.body());
                assertTrue(textDownload.headers().firstValue("Content-Disposition").orElse("").contains("budget"), "File name is kept");
                assertArrayEquals(binary, download(b, "/assets/" + binaryId + "/download", bob, 200).body());
                assertArrayEquals(image, download(b, "/users/" + bob + "/assets/" + imageId + "/download", bob, 200).body());
                assertArrayEquals(binary, download(b, "/assets/" + binaryId + "/download", bob, 200).body(), "Served again from Bob's local cache");
                try (var cached = Files.list(bobCache)) {
                    assertEquals(3, cached.filter(Files::isRegularFile).count(), "Downloads fill Bob's local cache");
                }

                // Bob was notified of every file, Alice of none of her own.
                long fileNotifications = 0;
                for (JsonNode notification : get(b, "/notifications/unread", bob, 200)) {
                    if ("FILE_ADDED".equals(notification.path("type").asText())) {
                        fileNotifications++;
                        assertTrue(Set.of("TASK", "PROJECT").contains(notification.path("referenceType").asText()), notification::toString);
                    }
                }
                assertEquals(3, fileNotifications, "Bob must be notified of the three files");
                for (JsonNode notification : get(a, "/notifications/unread", alice, 200)) {
                    assertNotEquals("FILE_ADDED", notification.path("type").asText());
                }

                // Non-members cannot list, download or upload.
                get(b, "/projects/" + project + "/assets", carol, 403);
                get(b, "/tasks/" + task + "/assets", carol, 403);
                get(b, "/assets/" + binaryId, carol, 403);
                download(b, "/assets/" + binaryId + "/download", carol, 403);
                download(b, "/users/" + carol + "/assets/" + textId + "/download", carol, 403);
                upload(b, "/projects/" + project + "/assets", carol, "intrusion.txt", "text/plain", text, 403);
                upload(b, "/tasks/" + task + "/assets", carol, "intrusion.txt", "text/plain", text, 403);
                upload(b, "/users/" + carol + "/assets/upload?taskId=" + task, carol, "intrusion.txt", "text/plain", text, 403);

                // Size limit gives a clear error.
                JsonNode tooLarge = upload(b, "/projects/" + project + "/assets", bob, "huge.pdf", "application/pdf", new byte[(int) MAX_FILE_SIZE + 1], 413);
                assertTrue(tooLarge.path("message").asText().contains("File troppo grande"), tooLarge::toString);

                // Collaboration goes both ways.
                byte[] reply = "Approved".getBytes(StandardCharsets.UTF_8);
                String replyId = upload(b, "/tasks/" + task + "/assets", bob, "approval.md", "text/markdown", reply, 201).get("id").asText();
                assertArrayEquals(reply, download(a, "/assets/" + replyId + "/download", alice, 200).body());
                assertTrue(stream(get(a, "/notifications/unread", alice, 200)).anyMatch(n -> "FILE_ADDED".equals(n.path("type").asText())
                        && task.equals(n.path("referenceId").asText())));

                // Only the uploader deletes, and deletion propagates to the other PC.
                delete(b, "/assets/" + binaryId, bob, 403);
                delete(b, "/assets/" + binaryId, carol, 403);
                delete(a, "/assets/" + binaryId, alice, 204);
                assertEquals(Set.of(textId, imageId, replyId), new HashSet<>(ids(get(b, "/projects/" + project + "/assets", bob, 200))));
                download(b, "/assets/" + binaryId + "/download", bob, 404);
                assertEquals(0, sql.queryForObject("SELECT COUNT(*) FROM asset_contents WHERE asset_id = ?", Integer.class, binaryId),
                        "Deleted files free the shared database");
            }
        } finally {
            database.stop();
        }
    }

    @Test
    void mariaDbProvisionedBeforeSharedFilesIsUpgradedByV3() throws Exception {
        // Schema as provisioned by 0.1.7: no asset_contents table, no assets.project_id.
        Path sqlite = temporaryDirectory.resolve("old-schema.db");
        Flyway.configure().dataSource("jdbc:sqlite:" + sqlite, null, null).locations("classpath:db/migration")
                .target("63").load().migrate();

        DBConfigurationBuilder configuration = DBConfigurationBuilder.newBuilder();
        configuration.setPort(0);
        configuration.setBaseDir(temporaryDirectory.resolve("old-mariadb-base").toFile());
        configuration.setDataDir(temporaryDirectory.resolve("old-mariadb-data").toFile());
        DB database = DB.newEmbeddedDB(configuration.build());
        database.start();
        try {
            String url = configuration.getURL("librepm_assets_upgrade") + "?createDatabaseIfNotExist=true";
            DriverManagerDataSource dataSource = new DriverManagerDataSource(url, "root", "");
            new SqliteToMariaDbMigrator().migrate(sqlite, dataSource, false);
            JdbcTemplate sql = new JdbcTemplate(dataSource);
            String tableQuery = "SELECT COUNT(*) FROM information_schema.tables WHERE table_schema = DATABASE() AND table_name = 'asset_contents'";
            assertEquals(0, sql.queryForObject(tableQuery, Integer.class));

            try (ConfigurableApplicationContext pc = start(url, "upgraded")) {
                assertEquals(1, sql.queryForObject(tableQuery, Integer.class));
                assertEquals("longblob", sql.queryForObject("SELECT data_type FROM information_schema.columns WHERE table_schema = DATABASE() " +
                        "AND table_name = 'asset_contents' AND column_name = 'data'", String.class));
                String a = base(pc);
                String owner = post(a, "/users", null, Map.of("username", "owner" + System.nanoTime(), "displayName", "Owner", "password", "password123"), 201).get("id").asText();
                String project = post(a, "/users/" + owner + "/projects", owner, Map.of("name", "Upgraded"), 201).get("id").asText();
                byte[] content = new byte[600_000];
                new Random(1).nextBytes(content);
                String asset = upload(a, "/projects/" + project + "/assets", owner, "after-upgrade.pdf", "application/pdf", content, 201).get("id").asText();
                assertEquals(2, sql.queryForObject("SELECT COUNT(*) FROM asset_contents WHERE asset_id = ?", Integer.class, asset));
                assertEquals(project, sql.queryForObject("SELECT project_id FROM assets WHERE id = ?", String.class, asset));
                assertArrayEquals(content, download(a, "/assets/" + asset + "/download", owner, 200).body());
            }
        } finally {
            database.stop();
        }
    }

    private ConfigurableApplicationContext start(String url, String client) {
        Path data = temporaryDirectory.resolve("client-" + client);
        return new SpringApplicationBuilder(LibrePMApplication.class)
                .profiles("mariadb")
                .properties("spring.task.scheduling.enabled=false", "librepm.remote-migration.enabled=false")
                .run("--spring.datasource.url=" + url,
                        "--spring.datasource.username=root", "--spring.datasource.password=",
                        "--server.port=0", "--librepm.data.path=" + data,
                        "--librepm.assets.max-file-size=" + MAX_FILE_SIZE,
                        "--logging.file.name=" + data.resolve("backend.log"));
    }

    private String base(ConfigurableApplicationContext context) {
        return "http://127.0.0.1:" + ((WebServerApplicationContext) context).getWebServer().getPort() + "/api";
    }

    private static List<String> ids(JsonNode array) {
        List<String> ids = new ArrayList<>();
        array.forEach(node -> ids.add(node.get("id").asText()));
        return ids;
    }

    private static java.util.stream.Stream<JsonNode> stream(JsonNode array) {
        List<JsonNode> nodes = new ArrayList<>();
        array.forEach(nodes::add);
        return nodes.stream();
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

    private HttpResponse<byte[]> download(String base, String path, String user, int status) throws Exception {
        HttpRequest request = HttpRequest.newBuilder(URI.create(base + path)).header("X-User-Id", user).GET().build();
        HttpResponse<byte[]> response = http.send(request, HttpResponse.BodyHandlers.ofByteArray());
        assertEquals(status, response.statusCode(), () -> "GET " + path + " -> " + new String(response.body(), StandardCharsets.UTF_8));
        return response;
    }

    static byte[] multipart(String boundary, String fileName, String mimeType, byte[] content) {
        ByteArrayOutputStream body = new ByteArrayOutputStream();
        String head = "--" + boundary + "\r\nContent-Disposition: form-data; name=\"file\"; filename=\"" + fileName + "\"\r\n" +
                "Content-Type: " + mimeType + "\r\n\r\n";
        body.writeBytes(head.getBytes(StandardCharsets.UTF_8));
        body.writeBytes(content);
        body.writeBytes(("\r\n--" + boundary + "--\r\n").getBytes(StandardCharsets.UTF_8));
        return body.toByteArray();
    }

    private JsonNode upload(String base, String path, String user, String fileName, String mimeType, byte[] content, int status) throws Exception {
        String boundary = "librepm-" + UUID.randomUUID();
        HttpRequest request = HttpRequest.newBuilder(URI.create(base + path))
                .header("X-User-Id", user)
                .header("Content-Type", "multipart/form-data; boundary=" + boundary)
                .POST(HttpRequest.BodyPublishers.ofByteArray(multipart(boundary, fileName, mimeType, content)))
                .build();
        HttpResponse<String> response = http.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(status, response.statusCode(), () -> "POST " + path + " -> " + response.body());
        return response.body().isBlank() ? json.nullNode() : json.readTree(response.body());
    }
}
