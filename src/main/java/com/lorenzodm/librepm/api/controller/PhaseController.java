package com.lorenzodm.librepm.api.controller;

import com.lorenzodm.librepm.core.entity.Phase;
import com.lorenzodm.librepm.service.PhaseService;
import com.lorenzodm.librepm.service.RecalculationService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/projects/{projectId}/phases")
public class PhaseController {

    private final PhaseService phaseService;
    private final RecalculationService recalculationService;

    public PhaseController(PhaseService phaseService, RecalculationService recalculationService) {
        this.phaseService = phaseService;
        this.recalculationService = recalculationService;
    }

    @PostMapping("/recalculate")
    public ResponseEntity<Map<String, String>> recalculate(@PathVariable String projectId) {
        recalculationService.recalculate(projectId);
        return ResponseEntity.ok(Map.of("status", "recalculated"));
    }

    @GetMapping
    public ResponseEntity<List<Map<String, Object>>> list(@PathVariable String projectId) {
        return ResponseEntity.ok(
                phaseService.listByProject(projectId).stream()
                        .map(this::toMap)
                        .toList()
        );
    }

    @PostMapping
    public ResponseEntity<Map<String, Object>> create(
            @PathVariable String projectId,
            @RequestBody Map<String, Object> body) {
        Phase phase = phaseService.create(
                projectId,
                (String) body.get("name"),
                (String) body.get("description"),
                parseDate(body.get("plannedStart")),
                parseDate(body.get("plannedEnd")),
                (String) body.get("color"),
                body.containsKey("sortOrder") ? ((Number) body.get("sortOrder")).intValue() : 0
        );
        return ResponseEntity.ok(toMap(phase));
    }

    @PutMapping("/{phaseId}")
    public ResponseEntity<Map<String, Object>> update(
            @PathVariable String phaseId,
            @RequestBody Map<String, Object> body) {
        Phase phase = phaseService.update(
                phaseId,
                (String) body.get("name"),
                (String) body.get("description"),
                parseDate(body.get("plannedStart")),
                parseDate(body.get("plannedEnd")),
                parseDate(body.get("actualStart")),
                parseDate(body.get("actualEnd")),
                (String) body.get("status"),
                (String) body.get("color"),
                body.containsKey("sortOrder") ? ((Number) body.get("sortOrder")).intValue() : 0
        );
        return ResponseEntity.ok(toMap(phase));
    }

    @DeleteMapping("/{phaseId}")
    public ResponseEntity<Void> delete(@PathVariable String phaseId) {
        phaseService.delete(phaseId);
        return ResponseEntity.noContent().build();
    }

    private LocalDate parseDate(Object val) {
        if (val == null) return null;
        return LocalDate.parse(val.toString());
    }

    private Map<String, Object> toMap(Phase p) {
        return Map.ofEntries(
                Map.entry("id", p.getId()),
                Map.entry("name", p.getName()),
                Map.entry("description", p.getDescription() != null ? p.getDescription() : ""),
                Map.entry("sortOrder", p.getSortOrder()),
                Map.entry("plannedStart", p.getPlannedStart() != null ? p.getPlannedStart().toString() : ""),
                Map.entry("plannedEnd", p.getPlannedEnd() != null ? p.getPlannedEnd().toString() : ""),
                Map.entry("actualStart", p.getActualStart() != null ? p.getActualStart().toString() : ""),
                Map.entry("actualEnd", p.getActualEnd() != null ? p.getActualEnd().toString() : ""),
                Map.entry("status", p.getStatus().name()),
                Map.entry("color", p.getColor() != null ? p.getColor() : "")
        );
    }
}
