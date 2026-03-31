package com.lorenzodm.librepm.api.controller;

import com.lorenzodm.librepm.api.dto.request.AddMemberRequest;
import com.lorenzodm.librepm.api.dto.request.CreateGhostUserRequest;
import com.lorenzodm.librepm.api.dto.response.ProjectMemberResponse;
import com.lorenzodm.librepm.api.dto.response.UserResponse;
import com.lorenzodm.librepm.api.mapper.UserMapper;
import com.lorenzodm.librepm.core.entity.ProjectMember;
import com.lorenzodm.librepm.core.entity.Role;
import com.lorenzodm.librepm.core.entity.User;
import com.lorenzodm.librepm.repository.RoleRepository;
import com.lorenzodm.librepm.service.ProjectService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/users/{userId}/projects/{projectId}/members")
public class ProjectMemberController {

    private final ProjectService projectService;
    private final UserMapper userMapper;
    private final RoleRepository roleRepository;

    public ProjectMemberController(ProjectService projectService, UserMapper userMapper, RoleRepository roleRepository) {
        this.projectService = projectService;
        this.userMapper = userMapper;
        this.roleRepository = roleRepository;
    }

    @GetMapping
    public ResponseEntity<List<ProjectMemberResponse>> listMembers(
            @PathVariable String userId,
            @PathVariable String projectId) {
        List<ProjectMember> members = projectService.getMembers(userId, projectId);

        // Batch-load role names for efficiency
        Set<String> roleIds = members.stream()
                .map(ProjectMember::getSystemRoleId)
                .filter(id -> id != null)
                .collect(Collectors.toSet());
        Map<String, String> roleNames = roleIds.isEmpty() ? Map.of() :
                roleRepository.findAllById(roleIds).stream()
                        .collect(Collectors.toMap(Role::getId, Role::getName));

        List<ProjectMemberResponse> response = members.stream()
                .map(m -> toResponse(m, roleNames))
                .collect(Collectors.toList());
        return ResponseEntity.ok(response);
    }

    @PostMapping
    public ResponseEntity<ProjectMemberResponse> addMember(
            @PathVariable String userId,
            @PathVariable String projectId,
            @Valid @RequestBody AddMemberRequest req) {
        ProjectMember member = projectService.addMember(userId, projectId, req.userId(), ProjectMember.Role.valueOf(req.role()));
        return ResponseEntity.ok(toResponse(member, Map.of()));
    }

    @PatchMapping("/{memberId}/role")
    public ResponseEntity<ProjectMemberResponse> updateMemberRole(
            @PathVariable String userId,
            @PathVariable String projectId,
            @PathVariable String memberId,
            @RequestBody Map<String, String> body) {
        String role = body.get("role");
        String systemRoleId = body.get("systemRoleId");
        ProjectMember member = projectService.updateMemberRole(userId, projectId, memberId, role, systemRoleId);

        Map<String, String> roleNames = Map.of();
        if (member.getSystemRoleId() != null) {
            roleRepository.findById(member.getSystemRoleId())
                    .ifPresent(r -> {});
            roleNames = roleRepository.findById(member.getSystemRoleId())
                    .map(r -> Map.of(r.getId(), r.getName()))
                    .orElse(Map.of());
        }
        return ResponseEntity.ok(toResponse(member, roleNames));
    }

    @DeleteMapping("/{memberId}")
    public ResponseEntity<Void> removeMember(
            @PathVariable String userId,
            @PathVariable String projectId,
            @PathVariable String memberId) {
        projectService.removeMember(userId, projectId, memberId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/ghosts")
    public ResponseEntity<UserResponse> createGhost(
            @PathVariable String userId,
            @PathVariable String projectId,
            @Valid @RequestBody CreateGhostUserRequest req) {
        User ghost = projectService.createGhostUserAndAddToProject(userId, projectId, req.username(), req.displayName());
        return ResponseEntity.ok(userMapper.toResponseLight(ghost));
    }

    private ProjectMemberResponse toResponse(ProjectMember m, Map<String, String> roleNames) {
        String sysRoleId = m.getSystemRoleId();
        String sysRoleName = sysRoleId != null ? roleNames.getOrDefault(sysRoleId, null) : null;
        return new ProjectMemberResponse(
                userMapper.toResponseLight(m.getUser()),
                m.getRole().name(),
                sysRoleId,
                sysRoleName,
                m.getCreatedAt()
        );
    }
}
