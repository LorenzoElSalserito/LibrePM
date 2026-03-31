package com.lorenzodm.librepm.api.controller;

import com.lorenzodm.librepm.core.entity.WorkspaceProfile;
import com.lorenzodm.librepm.repository.UserRepository;
import com.lorenzodm.librepm.repository.WorkspaceProfileRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/workspace-profiles")
public class WorkspaceProfileController {

    private final WorkspaceProfileRepository profileRepo;
    private final UserRepository userRepo;

    public WorkspaceProfileController(WorkspaceProfileRepository profileRepo, UserRepository userRepo) {
        this.profileRepo = profileRepo;
        this.userRepo = userRepo;
    }

    @GetMapping
    public ResponseEntity<List<Map<String, Object>>> list() {
        return ResponseEntity.ok(profileRepo.findAll().stream().map(this::toMap).toList());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Map<String, Object>> get(@PathVariable String id) {
        return profileRepo.findById(id)
                .map(p -> ResponseEntity.ok(toMap(p)))
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/users/{userId}/profile")
    public ResponseEntity<Map<String, Object>> assignToUser(
            @PathVariable String userId, @RequestBody Map<String, String> body) {
        var user = userRepo.findById(userId).orElseThrow();
        String profileId = body.get("profileId");
        user.setWorkspaceProfileId(profileId);
        userRepo.save(user);
        var profile = profileRepo.findById(profileId).orElseThrow();
        return ResponseEntity.ok(toMap(profile));
    }

    private Map<String, Object> toMap(WorkspaceProfile p) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id", p.getId());
        m.put("name", p.getName());
        m.put("description", p.getDescription());
        m.put("modulesJson", p.getModulesJson());
        m.put("navItemsJson", p.getNavItemsJson());
        m.put("isSystem", p.isSystem());
        return m;
    }
}
