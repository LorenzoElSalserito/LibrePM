package com.lorenzodm.librepm.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import java.util.Map;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class DeletionE2ETest {
    @Autowired MockMvc mvc;
    @Autowired ObjectMapper json;
    private String create(String path, Map<String, String> data, String user) throws Exception {
        var result = mvc.perform(post(path).header("X-User-Id", user).contentType(MediaType.APPLICATION_JSON).content(json.writeValueAsString(data)))
                .andExpect(status().isCreated()).andReturn();
        return json.readTree(result.getResponse().getContentAsString()).get("id").asText();
    }
    @Test void versionedTaskNoteAndProjectCanBeDeleted() throws Exception {
        String user = create("/api/users", Map.of("username", "delete-e2e", "displayName", "Delete Test", "password", "password123"), "unused");
        String projects = "/api/users/" + user + "/projects";
        String project = create(projects, Map.of("name", "Delete project"), user);
        String tasks = projects + "/" + project + "/tasks";
        String task = create(tasks, Map.of("title", "Delete task"), user);
        String note = create("/api/notes?parentType=PROJECT&parentId=" + project, Map.of("title", "Delete note", "content", "Content"), user);
        mvc.perform(delete("/api/notes/" + note).header("X-User-Id", user)).andExpect(status().isNoContent());
        mvc.perform(get("/api/notes/" + note).header("X-User-Id", user)).andExpect(status().isNotFound());
        mvc.perform(delete(tasks + "/" + task)).andExpect(status().isNoContent());
        mvc.perform(get(tasks + "/" + task)).andExpect(status().isNotFound());
        mvc.perform(delete(projects + "/" + project)).andExpect(status().isNoContent());
        mvc.perform(get(projects + "/" + project)).andExpect(status().isNotFound());
    }
}
