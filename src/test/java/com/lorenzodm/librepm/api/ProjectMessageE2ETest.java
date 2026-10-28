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
class ProjectMessageE2ETest {
    @Autowired MockMvc mvc;
    @Autowired ObjectMapper json;
    private String create(String path, Map<String, String> data) throws Exception {
        var result = mvc.perform(post(path).contentType(MediaType.APPLICATION_JSON).content(json.writeValueAsString(data)))
                .andExpect(status().isCreated()).andReturn();
        return json.readTree(result.getResponse().getContentAsString()).get("id").asText();
    }
    private String user(String name) throws Exception {
        return create("/api/users", Map.of("username", name, "displayName", name, "password", "password123"));
    }
    @Test void messagesPersistAndRespectMembershipRolesAndValidation() throws Exception {
        String owner = user("chat-owner"), editor = user("chat-editor"), stranger = user("chat-stranger");
        String project = create("/api/users/" + owner + "/projects", Map.of("name", "Messages E2E"));
        String base = "/api/users/" + owner + "/projects/" + project;
        String feed = base + "/messages";
        mvc.perform(post(base + "/members").contentType(MediaType.APPLICATION_JSON)
                .content(json.writeValueAsString(Map.of("userId", editor, "role", "EDITOR")))).andExpect(status().isOk());
        create(feed, Map.of("content", "  Persisted <script>message</script>  "));
        mvc.perform(get(feed)).andExpect(status().isOk()).andExpect(jsonPath("$[0].content").value("Persisted <script>message</script>"));
        String editorFeed = "/api/users/" + editor + "/projects/" + project + "/messages";
        mvc.perform(get(editorFeed)).andExpect(status().isOk()).andExpect(jsonPath("$[0].senderId").value(owner));
        create(editorFeed, Map.of("content", "Reply"));
        mvc.perform(get("/api/users/" + stranger + "/projects/" + project + "/messages")).andExpect(status().isForbidden());
        mvc.perform(post("/api/users/" + stranger + "/projects/" + project + "/messages").contentType(MediaType.APPLICATION_JSON)
                .content("{\"content\":\"intrusion\"}")).andExpect(status().isForbidden());
        for (String content : new String[]{" ", "x".repeat(4001)}) {
            mvc.perform(post(feed).contentType(MediaType.APPLICATION_JSON).content(json.writeValueAsString(Map.of("content", content))))
                    .andExpect(status().isBadRequest());
        }
        mvc.perform(get(feed + "?page=-1")).andExpect(status().isBadRequest());
        mvc.perform(patch(base + "/members/" + editor + "/role").contentType(MediaType.APPLICATION_JSON)
                .content("{\"role\":\"VIEWER\"}")).andExpect(status().isOk());
        mvc.perform(post(editorFeed).contentType(MediaType.APPLICATION_JSON).content("{\"content\":\"blocked\"}")).andExpect(status().isForbidden());
        mvc.perform(get(editorFeed)).andExpect(status().isOk());
    }
}
