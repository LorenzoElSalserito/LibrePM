package com.lorenzodm.librepm.api;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * JSON workspace export -> import round trip through the REST API: the data exported for one user
 * is recreated identically (projects, task hierarchy, statuses, tags, checklist) for another user,
 * and the CSV project export can be imported back into a project.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class WorkspaceTransferE2ETest {

    @Autowired MockMvc mvc;
    @Autowired ObjectMapper json;

    private JsonNode send(String method, String path, Object body, String user, int expectedStatus) throws Exception {
        var builder = switch (method) {
            case "POST" -> post(path);
            case "PUT" -> put(path);
            case "PATCH" -> patch(path);
            default -> get(path);
        };
        builder.header("X-User-Id", user == null ? "" : user);
        if (body != null) {
            builder.contentType(MediaType.APPLICATION_JSON)
                    .content(body instanceof String s ? s : json.writeValueAsString(body));
        }
        var result = mvc.perform(builder).andExpect(status().is(expectedStatus)).andReturn();
        String content = result.getResponse().getContentAsString();
        return content.isEmpty() ? null : json.readTree(content);
    }

    private String newUser(String name) throws Exception {
        return send("POST", "/api/users", Map.of("username", name + "-" + UUID.randomUUID().toString().substring(0, 8),
                "displayName", name, "password", "password123"), null, 201).get("id").asText();
    }

    @Test
    void exportedWorkspaceImportsBackWithSameData() throws Exception {
        String alice = newUser("ws-alice");
        String projects = "/api/users/" + alice + "/projects";
        String projectId = send("POST", projects, Map.of("name", "Roundtrip", "description", "JSON backup", "color", "#336699"), alice, 201).get("id").asText();
        String tasks = projects + "/" + projectId + "/tasks";

        String tagId = send("POST", "/api/tags", Map.of("name", "urgent", "color", "#ff0000"), alice, 201).get("id").asText();
        String parentId = send("POST", tasks, Map.of("title", "Parent task", "description", "line 1\nline 2",
                "deadline", "2026-11-30", "tagIds", List.of(tagId)), alice, 201).get("id").asText();
        send("POST", tasks, Map.of("title", "Child task", "parentTaskId", parentId,
                "plannedStart", "2026-11-02T09:00:00", "plannedFinish", "2026-11-05T18:00:00"), alice, 201);
        String itemId = send("POST", tasks + "/" + parentId + "/checklist", Map.of("text", "Check me"), alice, 201).get("id").asText();
        send("PUT", tasks + "/" + parentId + "/checklist/" + itemId, Map.of("done", true), alice, 200);
        String archivedProject = send("POST", projects, Map.of("name", "Old stuff"), alice, 201).get("id").asText();
        send("PATCH", projects + "/" + archivedProject + "/archived?archived=true", null, alice, 200);

        JsonNode exported = send("GET", "/api/users/" + alice + "/workspace/export", null, alice, 200);
        assertThat(exported.get("format").asText()).isEqualTo("librepm-workspace");
        assertThat(exported.get("projects")).hasSize(2);

        // Restore into an emptied workspace: delete everything, import the file, compare.
        for (JsonNode p : exported.get("projects")) {
            mvc.perform(delete(projects + "/" + p.get("ref").asText()).header("X-User-Id", alice))
                    .andExpect(status().isNoContent());
        }
        assertThat(send("GET", "/api/users/" + alice + "/workspace/export", null, alice, 200).get("projects")).isEmpty();

        JsonNode summary = send("POST", "/api/users/" + alice + "/workspace/import", exported.toString(), alice, 200);
        assertThat(summary.get("projects").asInt()).isEqualTo(2);
        assertThat(summary.get("tasks").asInt()).isEqualTo(2);
        assertThat(summary.get("checklistItems").asInt()).isEqualTo(1);
        assertThat(summary.get("warnings")).isEmpty();

        JsonNode reExported = send("GET", "/api/users/" + alice + "/workspace/export", null, alice, 200);
        assertThat(normalize(reExported)).isEqualTo(normalize(exported));

        // Importing again is additive and leaves existing data untouched.
        send("POST", "/api/users/" + alice + "/workspace/import", exported.toString(), alice, 200);
        assertThat(send("GET", "/api/users/" + alice + "/workspace/export", null, alice, 200).get("projects")).hasSize(4);

        // Another profile gets the same projects and tasks; the globally-unique tag is reported.
        String bob = newUser("ws-bob");
        JsonNode bobSummary = send("POST", "/api/users/" + bob + "/workspace/import", exported.toString(), bob, 200);
        assertThat(bobSummary.get("projects").asInt()).isEqualTo(2);
        assertThat(bobSummary.get("tasks").asInt()).isEqualTo(2);
        assertThat(bobSummary.get("warnings").get(0).asText()).contains("urgent");
    }

    @Test
    void rejectsInvalidFilesWithoutPartialImport() throws Exception {
        String carol = newUser("ws-carol");
        String path = "/api/users/" + carol + "/workspace/import";
        send("POST", path, Map.of("format", "something-else", "projects", List.of()), carol, 400);
        send("POST", path, Map.of("format", "librepm-workspace"), carol, 400);
        send("POST", path, Map.of("format", "librepm-workspace", "formatVersion", 99, "projects", List.of()), carol, 400);
        // Second project is invalid: the first one must be rolled back too.
        send("POST", path, Map.of("projects", List.of(Map.of("name", "Valid"), Map.of("description", "no name"))), carol, 400);
        assertThat(send("GET", "/api/users/" + carol + "/workspace/export", null, carol, 200).get("projects")).isEmpty();
    }

    @Test
    void projectCsvExportImportsBack() throws Exception {
        String dave = newUser("ws-dave");
        String projects = "/api/users/" + dave + "/projects";
        String source = send("POST", projects, Map.of("name", "CSV source"), dave, 201).get("id").asText();
        send("POST", projects + "/" + source + "/tasks", Map.of("title", "Quoted, \"task\"", "description", "multi\nline", "deadline", "2026-12-01"), dave, 201);
        send("POST", projects + "/" + source + "/tasks", Map.of("title", "Plain task"), dave, 201);

        JsonNode job = send("POST", projects + "/" + source + "/export/csv", null, dave, 200);
        String csv = job.get("resultPayload").asText();

        String target = send("POST", projects, Map.of("name", "CSV target"), dave, 201).get("id").asText();
        var result = mvc.perform(post(projects + "/" + target + "/import/csv").header("X-User-Id", dave)
                .contentType(MediaType.TEXT_PLAIN).content(csv)).andExpect(status().isOk()).andReturn();
        JsonNode importJob = json.readTree(result.getResponse().getContentAsString());
        assertThat(importJob.get("status").asText()).isEqualTo("COMPLETED");
        assertThat(importJob.get("recordCount").asInt()).isEqualTo(2);

        JsonNode workspace = send("GET", "/api/users/" + dave + "/workspace/export", null, dave, 200);
        JsonNode imported = null;
        for (JsonNode p : workspace.get("projects")) {
            if (p.get("ref").asText().equals(target)) imported = p.get("tasks");
        }
        assertThat(imported).isNotNull();
        Set<String> titles = new HashSet<>();
        imported.forEach(t -> titles.add(t.get("title").asText()));
        assertThat(titles).containsExactlyInAnyOrder("Quoted, \"task\"", "Plain task");
        imported.forEach(t -> {
            if (t.get("title").asText().startsWith("Quoted")) {
                assertThat(t.get("description").asText()).isEqualTo("multi\nline");
                assertThat(t.get("deadline").asText()).isEqualTo("2026-12-01");
            }
        });
    }

    /** Removes volatile fields (ids, timestamps) so two exports can be compared by content. */
    private JsonNode normalize(JsonNode doc) {
        var copy = doc.deepCopy();
        var root = (com.fasterxml.jackson.databind.node.ObjectNode) copy;
        root.remove("exportedAt");
        List<JsonNode> projects = new ArrayList<>();
        for (JsonNode p : root.get("projects")) {
            var pn = (com.fasterxml.jackson.databind.node.ObjectNode) p;
            Map<String, String> refToTitle = new HashMap<>();
            p.get("tasks").forEach(t -> refToTitle.put(t.get("ref").asText(), t.get("title").asText()));
            pn.remove("ref");
            List<JsonNode> taskList = new ArrayList<>();
            for (JsonNode t : p.get("tasks")) {
                var tn = (com.fasterxml.jackson.databind.node.ObjectNode) t;
                String parent = t.hasNonNull("parentRef") ? refToTitle.get(t.get("parentRef").asText()) : null;
                tn.remove("ref");
                tn.put("parentRef", parent);
                taskList.add(tn);
            }
            taskList.sort(Comparator.comparing(t -> t.get("title").asText()));
            pn.set("tasks", json.valueToTree(taskList));
            projects.add(pn);
        }
        projects.sort(Comparator.comparing(p -> p.get("name").asText()));
        root.set("projects", json.valueToTree(projects));
        return root;
    }
}
