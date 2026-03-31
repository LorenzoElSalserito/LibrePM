package com.lorenzodm.librepm.api.controller;

import com.lorenzodm.librepm.api.exception.ResourceNotFoundException;
import com.lorenzodm.librepm.core.entity.*;
import com.lorenzodm.librepm.repository.*;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.*;

/**
 * Portfolio REST controller (Phase 22 / PRD-22).
 * Manages programmes, memberships, and milestones.
 */
@RestController
@RequestMapping("/api/programmes")
public class PortfolioController {

    private final ProgrammeRepository progRepo;
    private final ProgrammeMembershipRepository memberRepo;
    private final ProgrammeMilestoneRepository milestoneRepo;
    private final ProjectRepository projectRepo;

    public PortfolioController(ProgrammeRepository progRepo,
                               ProgrammeMembershipRepository memberRepo,
                               ProgrammeMilestoneRepository milestoneRepo,
                               ProjectRepository projectRepo) {
        this.progRepo = progRepo;
        this.memberRepo = memberRepo;
        this.milestoneRepo = milestoneRepo;
        this.projectRepo = projectRepo;
    }

    // =========== PROGRAMMES ===========

    @GetMapping
    public ResponseEntity<List<Map<String, Object>>> listProgrammes() {
        return ResponseEntity.ok(progRepo.findAllByOrderByNameAsc()
                .stream().map(this::progToMap).toList());
    }

    @PostMapping
    public ResponseEntity<Map<String, Object>> createProgramme(@RequestBody Map<String, Object> body) {
        Programme p = new Programme();
        p.setName((String) body.getOrDefault("name", "New Programme"));
        p.setDescription((String) body.get("description"));
        p.setOwnerId((String) body.get("ownerId"));
        p.setStatus((String) body.getOrDefault("status", "ACTIVE"));
        return ResponseEntity.ok(progToMap(progRepo.save(p)));
    }

    @PutMapping("/{progId}")
    public ResponseEntity<Map<String, Object>> updateProgramme(
            @PathVariable String progId, @RequestBody Map<String, Object> body) {
        Programme p = progRepo.findById(progId)
                .orElseThrow(() -> new ResourceNotFoundException("Programme not found: " + progId));
        if (body.containsKey("name")) p.setName((String) body.get("name"));
        if (body.containsKey("description")) p.setDescription((String) body.get("description"));
        if (body.containsKey("status")) p.setStatus((String) body.get("status"));
        if (body.containsKey("ownerId")) p.setOwnerId((String) body.get("ownerId"));
        return ResponseEntity.ok(progToMap(progRepo.save(p)));
    }

    @DeleteMapping("/{progId}")
    public ResponseEntity<Void> deleteProgramme(@PathVariable String progId) {
        progRepo.deleteById(progId);
        return ResponseEntity.noContent().build();
    }

    // =========== MEMBERSHIPS ===========

    @GetMapping("/{progId}/projects")
    public ResponseEntity<List<Map<String, Object>>> listMembers(@PathVariable String progId) {
        return ResponseEntity.ok(memberRepo.findByProgrammeIdOrderBySortOrderAsc(progId)
                .stream().map(this::memberToMap).toList());
    }

    @PostMapping("/{progId}/projects")
    public ResponseEntity<Map<String, Object>> addProject(
            @PathVariable String progId, @RequestBody Map<String, Object> body) {
        ProgrammeMembership m = new ProgrammeMembership();
        m.setProgrammeId(progId);
        m.setProjectId((String) body.get("projectId"));
        if (body.get("sortOrder") != null) m.setSortOrder(((Number) body.get("sortOrder")).intValue());
        return ResponseEntity.ok(memberToMap(memberRepo.save(m)));
    }

    @DeleteMapping("/{progId}/projects/{membershipId}")
    public ResponseEntity<Void> removeProject(@PathVariable String progId, @PathVariable String membershipId) {
        memberRepo.deleteById(membershipId);
        return ResponseEntity.noContent().build();
    }

    // =========== MILESTONES ===========

    @GetMapping("/{progId}/milestones")
    public ResponseEntity<List<Map<String, Object>>> listMilestones(@PathVariable String progId) {
        return ResponseEntity.ok(milestoneRepo.findByProgrammeIdOrderByTargetDateAsc(progId)
                .stream().map(this::milestoneToMap).toList());
    }

    @PostMapping("/{progId}/milestones")
    public ResponseEntity<Map<String, Object>> createMilestone(
            @PathVariable String progId, @RequestBody Map<String, Object> body) {
        ProgrammeMilestone ms = new ProgrammeMilestone();
        ms.setProgrammeId(progId);
        ms.setName((String) body.getOrDefault("name", "New Milestone"));
        ms.setDescription((String) body.get("description"));
        if (body.get("targetDate") != null) ms.setTargetDate(LocalDate.parse((String) body.get("targetDate")));
        ms.setStatus((String) body.getOrDefault("status", "PENDING"));
        ms.setLinkedProjectId((String) body.get("linkedProjectId"));
        return ResponseEntity.ok(milestoneToMap(milestoneRepo.save(ms)));
    }

    @PutMapping("/{progId}/milestones/{msId}")
    public ResponseEntity<Map<String, Object>> updateMilestone(
            @PathVariable String progId, @PathVariable String msId,
            @RequestBody Map<String, Object> body) {
        ProgrammeMilestone ms = milestoneRepo.findById(msId)
                .orElseThrow(() -> new ResourceNotFoundException("ProgrammeMilestone not found: " + msId));
        if (body.containsKey("name")) ms.setName((String) body.get("name"));
        if (body.containsKey("description")) ms.setDescription((String) body.get("description"));
        if (body.containsKey("targetDate")) ms.setTargetDate(body.get("targetDate") != null ? LocalDate.parse((String) body.get("targetDate")) : null);
        if (body.containsKey("status")) ms.setStatus((String) body.get("status"));
        if (body.containsKey("linkedProjectId")) ms.setLinkedProjectId((String) body.get("linkedProjectId"));
        return ResponseEntity.ok(milestoneToMap(milestoneRepo.save(ms)));
    }

    @DeleteMapping("/{progId}/milestones/{msId}")
    public ResponseEntity<Void> deleteMilestone(@PathVariable String progId, @PathVariable String msId) {
        milestoneRepo.deleteById(msId);
        return ResponseEntity.noContent().build();
    }

    // =========== OVERVIEW ===========

    @GetMapping("/{progId}/overview")
    public ResponseEntity<Map<String, Object>> getOverview(@PathVariable String progId) {
        Programme prog = progRepo.findById(progId)
                .orElseThrow(() -> new ResourceNotFoundException("Programme not found: " + progId));
        List<ProgrammeMembership> members = memberRepo.findByProgrammeIdOrderBySortOrderAsc(progId);
        List<ProgrammeMilestone> milestones = milestoneRepo.findByProgrammeIdOrderByTargetDateAsc(progId);

        // Aggregate project data
        List<Map<String, Object>> projectSummaries = new ArrayList<>();
        for (ProgrammeMembership m : members) {
            projectRepo.findById(m.getProjectId()).ifPresent(proj -> {
                Map<String, Object> ps = new LinkedHashMap<>();
                ps.put("id", proj.getId());
                ps.put("name", proj.getName());
                ps.put("health", proj.getHealth() != null ? proj.getHealth().name() : "OK");
                ps.put("archived", proj.isArchived());
                ps.put("color", proj.getColor());
                projectSummaries.add(ps);
            });
        }

        Map<String, Object> overview = new LinkedHashMap<>();
        overview.put("programme", progToMap(prog));
        overview.put("projectCount", members.size());
        overview.put("projects", projectSummaries);
        overview.put("milestones", milestones.stream().map(this::milestoneToMap).toList());
        overview.put("milestonesCompleted", milestones.stream().filter(ms -> "COMPLETED".equals(ms.getStatus())).count());
        overview.put("milestonesTotal", milestones.size());

        return ResponseEntity.ok(overview);
    }

    // =========== MAPPERS ===========

    private Map<String, Object> progToMap(Programme p) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id", p.getId());
        m.put("name", p.getName());
        m.put("description", p.getDescription());
        m.put("ownerId", p.getOwnerId());
        m.put("status", p.getStatus());
        m.put("createdAt", p.getCreatedAt());
        return m;
    }

    private Map<String, Object> memberToMap(ProgrammeMembership mem) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id", mem.getId());
        m.put("programmeId", mem.getProgrammeId());
        m.put("projectId", mem.getProjectId());
        m.put("sortOrder", mem.getSortOrder());
        return m;
    }

    private Map<String, Object> milestoneToMap(ProgrammeMilestone ms) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id", ms.getId());
        m.put("programmeId", ms.getProgrammeId());
        m.put("name", ms.getName());
        m.put("description", ms.getDescription());
        m.put("targetDate", ms.getTargetDate());
        m.put("status", ms.getStatus());
        m.put("linkedProjectId", ms.getLinkedProjectId());
        return m;
    }
}
