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
import org.springframework.jdbc.datasource.DriverManagerDataSource;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Two desktop backends share one real MariaDB: notes written by a collaborator on
 * one client must be readable, notified and protected on the other.
 */
class CollaborativeNotesMariaDbE2ETest {
    private final HttpClient http = HttpClient.newHttpClient();
    private final ObjectMapper json = new ObjectMapper();

    @TempDir
    Path temporaryDirectory;

    @Test
    void notesOnTasksAndProjectsAreSharedBetweenCollaboratorsOnRemoteDatabase() throws Exception {
        Path sqlite = temporaryDirectory.resolve("schema.db");
        Flyway.configure().dataSource("jdbc:sqlite:" + sqlite, null, null).locations("classpath:db/migration").load().migrate();

        DBConfigurationBuilder configuration = DBConfigurationBuilder.newBuilder();
        configuration.setPort(0);
        configuration.setBaseDir(temporaryDirectory.resolve("mariadb-base").toFile());
        configuration.setDataDir(temporaryDirectory.resolve("mariadb-data").toFile());
        DB database = DB.newEmbeddedDB(configuration.build());
        database.start();
        try {
            String url = configuration.getURL("librepm_notes_e2e") + "?createDatabaseIfNotExist=true";
            new SqliteToMariaDbMigrator().migrate(sqlite, new DriverManagerDataSource(url, "root", ""), false);

            try (ConfigurableApplicationContext alicePc = start(url, "alice");
                 ConfigurableApplicationContext bobPc = start(url, "bob")) {
                String a = base(alicePc);
                String b = base(bobPc);
                long stamp = System.nanoTime();

                String alice = post(a, "/users", null, Map.of("username", "alice" + stamp, "displayName", "Alice", "password", "password123"), 201).get("id").asText();
                String bob = post(a, "/users", null, Map.of("username", "bob" + stamp, "displayName", "Bob", "password", "password123"), 201).get("id").asText();
                String carol = post(b, "/users", null, Map.of("username", "carol" + stamp, "displayName", "Carol", "password", "password123"), 201).get("id").asText();

                String project = post(a, "/users/" + alice + "/projects", alice, Map.of("name", "Shared project"), 201).get("id").asText();
                String task = post(a, "/users/" + alice + "/projects/" + project + "/tasks", alice, Map.of("title", "Shared task"), 201).get("id").asText();
                post(a, "/users/" + alice + "/projects/" + project + "/members", alice, Map.of("userId", bob, "role", "EDITOR"), 200);

                // Alice writes on her client.
                String taskNote = post(a, "/tasks/" + task + "/notes", alice, Map.of("title", "Task note", "content", "Check the <b>budget</b>"), 201).get("id").asText();
                String projectNote = post(a, "/projects/" + project + "/notes", alice, Map.of("title", "Project note", "content", "Kick-off on Monday"), 201).get("id").asText();

                // Bob reads on his own client, backed by the same database.
                assertEquals(List.of(taskNote), ids(get(b, "/tasks/" + task + "/notes", bob, 200)));
                assertEquals(List.of(projectNote), ids(get(b, "/projects/" + project + "/notes", bob, 200)));
                JsonNode read = get(b, "/notes/" + taskNote, bob, 200);
                assertEquals("Check the <b>budget</b>", read.get("content").asText());
                assertEquals(alice, read.get("owner").get("id").asText());
                assertTrue(ids(get(b, "/notes/feed?scope=INBOX", bob, 200)).containsAll(List.of(taskNote, projectNote)));
                long noteNotifications = 0;
                for (JsonNode notification : get(b, "/notifications/unread", bob, 200)) {
                    if ("NEW_NOTE".equals(notification.path("type").asText())) noteNotifications++;
                }
                assertEquals(2, noteNotifications, "Bob must be notified of both notes");

                // Collaboration goes both ways and edits are not served stale.
                String reply = post(b, "/tasks/" + task + "/notes", bob, Map.of("title", "Reply", "content", "Budget approved"), 201).get("id").asText();
                assertEquals(List.of(taskNote, reply), ids(get(a, "/tasks/" + task + "/notes", alice, 200)));
                put(a, "/notes/" + projectNote, alice, Map.of("content", "Kick-off moved to Tuesday"), 200);
                assertEquals("Kick-off moved to Tuesday", get(b, "/notes/" + projectNote, bob, 200).get("content").asText());

                // Ownership and membership stay enforced across clients.
                put(b, "/notes/" + projectNote, bob, Map.of("content", "Hijacked"), 403);
                delete(b, "/notes/" + taskNote, bob, 403);
                get(b, "/tasks/" + task + "/notes", carol, 403);
                get(b, "/projects/" + project + "/notes", carol, 403);
                get(b, "/notes/" + taskNote, carol, 403);
                post(b, "/projects/" + project + "/notes", carol, Map.of("title", "Intrusion"), 403);
                assertTrue(get(b, "/notes/feed?scope=ALL", carol, 200).isEmpty());

                // Backlinks are shared with members only, and only the author links a note.
                String link = post(a, "/note-links", alice, Map.of("noteId", projectNote, "linkedEntityType", "TASK", "linkedEntityId", task), 201).get("id").asText();
                assertEquals(List.of(projectNote), ids(get(b, "/notes/backlinks?entityType=TASK&entityId=" + task, bob, 200)));
                assertEquals(List.of(link), ids(get(b, "/note-links/entity/TASK/" + task, bob, 200)));
                assertTrue(get(b, "/notes/backlinks?entityType=TASK&entityId=" + task, carol, 200).isEmpty());
                assertTrue(get(b, "/note-links/entity/TASK/" + task, carol, 200).isEmpty());
                get(b, "/note-links/note/" + projectNote, carol, 403);
                post(b, "/note-links", bob, Map.of("noteId", projectNote, "linkedEntityType", "PROJECT", "linkedEntityId", project), 403);
                post(b, "/note-links", carol, Map.of("noteId", projectNote, "linkedEntityType", "PROJECT", "linkedEntityId", project), 403);
                delete(b, "/note-links/" + link, bob, 403);
                delete(a, "/note-links/" + link, alice, 204);

                // Project messages travel between clients as well.
                post(a, "/users/" + alice + "/projects/" + project + "/messages", alice, Map.of("content", "Stand-up at 9"), 201);
                post(b, "/users/" + bob + "/projects/" + project + "/messages", bob, Map.of("content", "On my way"), 201);
                JsonNode thread = get(a, "/users/" + alice + "/projects/" + project + "/messages", alice, 200);
                assertEquals(List.of("On my way", "Stand-up at 9"), List.of(thread.get(0).get("content").asText(), thread.get(1).get("content").asText()));
                assertEquals(2, get(b, "/users/" + bob + "/projects/" + project + "/messages", bob, 200).size());
                assertEquals("Bob", thread.get(0).get("senderName").asText(), "collaborators see who wrote, not an id");
                assertEquals("Alice", thread.get(1).get("senderName").asText());
                get(b, "/users/" + carol + "/projects/" + project + "/messages", carol, 403);
                post(b, "/users/" + carol + "/projects/" + project + "/messages", carol, Map.of("content", "Intrusion"), 403);

                // Deletion by the owner propagates to the collaborator.
                delete(b, "/notes/" + reply, bob, 204);
                assertEquals(List.of(taskNote), ids(get(a, "/tasks/" + task + "/notes", alice, 200)));
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
                        "--logging.file.name=" + data.resolve("backend.log"));
    }

    private String base(ConfigurableApplicationContext context) {
        return "http://127.0.0.1:" + ((WebServerApplicationContext) context).getWebServer().getPort() + "/api";
    }

    private List<String> ids(JsonNode array) {
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

    private JsonNode put(String base, String path, String user, Object body, int status) throws Exception {
        return send(HttpRequest.newBuilder(URI.create(base + path)).PUT(HttpRequest.BodyPublishers.ofString(json.writeValueAsString(body))), user, status);
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
}
