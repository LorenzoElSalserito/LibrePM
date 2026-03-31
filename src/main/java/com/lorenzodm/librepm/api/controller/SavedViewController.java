package com.lorenzodm.librepm.api.controller;

import com.lorenzodm.librepm.core.entity.SavedView;
import com.lorenzodm.librepm.service.SavedViewService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

/**
 * REST controller for saved views (PRD-01-FR-007, PRD-10-FR-003).
 *
 * @author Lorenzo DM
 * @since 0.10.0
 */
@RestController
@RequestMapping("/api/users/{userId}/views")
public class SavedViewController {

    private final SavedViewService savedViewService;

    public SavedViewController(SavedViewService savedViewService) {
        this.savedViewService = savedViewService;
    }

    @GetMapping
    public ResponseEntity<List<SavedView>> listAll(@PathVariable String userId) {
        return ResponseEntity.ok(savedViewService.listAll(userId));
    }

    @GetMapping("/project/{projectId}")
    public ResponseEntity<List<SavedView>> listByProject(
            @PathVariable String userId,
            @PathVariable String projectId) {
        return ResponseEntity.ok(savedViewService.listByProject(userId, projectId));
    }

    @GetMapping("/global")
    public ResponseEntity<List<SavedView>> listGlobal(@PathVariable String userId) {
        return ResponseEntity.ok(savedViewService.listGlobal(userId));
    }

    @PostMapping
    public ResponseEntity<SavedView> create(
            @PathVariable String userId,
            @RequestBody SavedView view) {
        SavedView created = savedViewService.create(userId, view);
        return ResponseEntity.created(URI.create("/api/users/" + userId + "/views/" + created.getId()))
                .body(created);
    }

    @PutMapping("/{viewId}")
    public ResponseEntity<SavedView> update(
            @PathVariable String userId,
            @PathVariable String viewId,
            @RequestBody SavedView view) {
        return ResponseEntity.ok(savedViewService.update(userId, viewId, view));
    }

    @DeleteMapping("/{viewId}")
    public ResponseEntity<Void> delete(
            @PathVariable String userId,
            @PathVariable String viewId) {
        savedViewService.delete(userId, viewId);
        return ResponseEntity.noContent().build();
    }
}
