package com.lorenzodm.librepm.api.controller;

import com.lorenzodm.librepm.api.dto.request.CreateGhostUserRequest;
import com.lorenzodm.librepm.api.dto.response.UserResponse;
import com.lorenzodm.librepm.api.mapper.UserMapper;
import com.lorenzodm.librepm.core.entity.Team;
import com.lorenzodm.librepm.core.entity.TeamMember;
import com.lorenzodm.librepm.core.entity.User;
import com.lorenzodm.librepm.service.TeamService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/teams")
public class TeamController {

    private final TeamService teamService;
    private final UserMapper userMapper;

    public TeamController(TeamService teamService, UserMapper userMapper) {
        this.teamService = teamService;
        this.userMapper = userMapper;
    }

    @PostMapping
    public ResponseEntity<Team> createTeam(@RequestBody Map<String, String> payload) {
        String name = payload.get("name");
        String description = payload.get("description");
        String ownerId = payload.get("ownerId");
        return ResponseEntity.ok(teamService.createTeam(name, description, ownerId));
    }

    @GetMapping
    public ResponseEntity<List<Team>> getMyTeams(@RequestParam String userId) {
        return ResponseEntity.ok(teamService.getTeamsForUser(userId));
    }

    @GetMapping("/{teamId}")
    public ResponseEntity<Team> getTeam(@PathVariable String teamId) {
        return ResponseEntity.ok(teamService.getTeam(teamId));
    }

    @PostMapping("/{teamId}/members")
    public ResponseEntity<Void> addMember(@PathVariable String teamId, @RequestBody Map<String, String> payload) {
        String userId = payload.get("userId");
        String roleStr = payload.getOrDefault("role", "TEAM_MEMBER");
        TeamMember.Role role = TeamMember.Role.valueOf(roleStr);
        teamService.addMember(teamId, userId, role);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{teamId}/members/{userId}")
    public ResponseEntity<Void> removeMember(@PathVariable String teamId, @PathVariable String userId) {
        teamService.removeMember(teamId, userId);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{teamId}/ghosts")
    public ResponseEntity<UserResponse> createGhostMember(
            @PathVariable String teamId,
            @Valid @RequestBody CreateGhostUserRequest req) {
        User ghost = teamService.createGhostMember(teamId, req.username(), req.displayName());
        return ResponseEntity.ok(userMapper.toResponseLight(ghost));
    }
}
