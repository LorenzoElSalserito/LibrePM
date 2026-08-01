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
import org.springframework.test.web.servlet.MvcResult;

import java.util.Map;

import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.not;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ProjectMemberE2ETest {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void projectTeamMembershipFlowWorksEndToEnd() throws Exception {
        String ownerId = createUser("owner.team", "owner.team@example.test", "Owner Team");
        String memberId = createUser("member.team", "member.team@example.test", "Member Team");

        String projectId = createProject(ownerId, "Team Flow Project");

        mvc.perform(get(membersPath(ownerId, projectId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].user.id").value(ownerId))
                .andExpect(jsonPath("$[0].role").value("OWNER"));

        mvc.perform(post(membersPath(ownerId, projectId))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("userId", memberId, "role", "VIEWER"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.user.id").value(memberId))
                .andExpect(jsonPath("$.role").value("VIEWER"));

        mvc.perform(get(membersPath(memberId, projectId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)));

        mvc.perform(patch(membersPath(memberId, projectId) + "/" + memberId + "/role")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("role", "ADMIN"))))
                .andExpect(status().isForbidden());

        mvc.perform(patch(membersPath(ownerId, projectId) + "/" + memberId + "/role")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("role", "ADMIN"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.role").value("ADMIN"));

        mvc.perform(post(membersPath(ownerId, projectId))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("userId", memberId, "role", "VIEWER"))))
                .andExpect(status().isConflict());

        mvc.perform(post(membersPath(ownerId, projectId))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("userId", memberId, "role", "INVALID"))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("BAD_REQUEST"));

        mvc.perform(post(membersPath(ownerId, projectId) + "/ghosts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of(
                                "username", "local.team.viewer",
                                "displayName", "Local Team Viewer",
                                "role", "VIEWER"
                        ))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.ghost").value(true));

        mvc.perform(get(membersPath(ownerId, projectId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[?(@.user.username == 'local.team.viewer')]", not(empty())))
                .andExpect(jsonPath("$[?(@.user.username == 'local.team.viewer')].role").value("VIEWER"))
                .andExpect(jsonPath("$[?(@.user.username == 'local.team.viewer')].user.ghost").value(true));

        mvc.perform(delete(membersPath(ownerId, projectId) + "/" + memberId))
                .andExpect(status().isNoContent());

        mvc.perform(get(membersPath(ownerId, projectId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[?(@.user.id == '" + memberId + "')]").isEmpty());

        mvc.perform(delete(membersPath(ownerId, projectId) + "/" + ownerId))
                .andExpect(status().isConflict());
    }

    private String createUser(String username, String email, String displayName) throws Exception {
        MvcResult result = mvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of(
                                "username", username,
                                "email", email,
                                "password", "password123",
                                "displayName", displayName
                        ))))
                .andExpect(status().isCreated())
                .andReturn();
        return idFrom(result);
    }

    private String createProject(String ownerId, String name) throws Exception {
        MvcResult result = mvc.perform(post("/api/users/" + ownerId + "/projects")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("name", name, "visibility", "PERSONAL"))))
                .andExpect(status().isCreated())
                .andReturn();
        return idFrom(result);
    }

    private String membersPath(String userId, String projectId) {
        return "/api/users/" + userId + "/projects/" + projectId + "/members";
    }

    private String json(Object value) throws Exception {
        return objectMapper.writeValueAsString(value);
    }

    private String idFrom(MvcResult result) throws Exception {
        JsonNode json = objectMapper.readTree(result.getResponse().getContentAsString());
        return json.get("id").asText();
    }
}
