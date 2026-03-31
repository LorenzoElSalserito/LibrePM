package com.lorenzodm.librepm.api.controller;

import com.lorenzodm.librepm.core.entity.StatusReview;
import com.lorenzodm.librepm.repository.ProjectRepository;
import com.lorenzodm.librepm.repository.UserRepository;
import com.lorenzodm.librepm.service.StatusReviewService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/projects/{projectId}/status-reviews")
public class StatusReviewController {

    private final StatusReviewService statusReviewService;
    private final ProjectRepository projectRepo;
    private final UserRepository userRepo;

    public StatusReviewController(StatusReviewService statusReviewService,
                                  ProjectRepository projectRepo, UserRepository userRepo) {
        this.statusReviewService = statusReviewService;
        this.projectRepo = projectRepo;
        this.userRepo = userRepo;
    }

    @GetMapping
    public ResponseEntity<List<Map<String, Object>>> list(@PathVariable String projectId) {
        return ResponseEntity.ok(
                statusReviewService.listByProject(projectId).stream().map(this::toMap).toList()
        );
    }

    @GetMapping("/latest")
    public ResponseEntity<Map<String, Object>> getLatest(@PathVariable String projectId) {
        return statusReviewService.getLatest(projectId)
                .map(sr -> ResponseEntity.ok(toMap(sr)))
                .orElse(ResponseEntity.noContent().build());
    }

    @PostMapping
    public ResponseEntity<Map<String, Object>> create(
            @PathVariable String projectId, @RequestBody Map<String, Object> body) {
        StatusReview sr = new StatusReview();
        sr.setProject(projectRepo.findById(projectId).orElseThrow());
        sr.setReviewer(userRepo.findById((String) body.get("reviewerId")).orElseThrow());
        sr.setReviewDate(body.get("reviewDate") != null ? LocalDate.parse((String) body.get("reviewDate")) : LocalDate.now());
        sr.setOverallStatus(StatusReview.TrafficLight.valueOf((String) body.get("overallStatus")));
        if (body.get("scheduleStatus") != null) sr.setScheduleStatus(StatusReview.TrafficLight.valueOf((String) body.get("scheduleStatus")));
        if (body.get("budgetStatus") != null) sr.setBudgetStatus(StatusReview.TrafficLight.valueOf((String) body.get("budgetStatus")));
        if (body.get("riskStatus") != null) sr.setRiskStatus(StatusReview.TrafficLight.valueOf((String) body.get("riskStatus")));
        if (body.get("summary") != null) sr.setSummary((String) body.get("summary"));
        if (body.get("achievements") != null) sr.setAchievements((String) body.get("achievements"));
        if (body.get("blockers") != null) sr.setBlockers((String) body.get("blockers"));
        if (body.get("nextActions") != null) sr.setNextActions((String) body.get("nextActions"));
        return ResponseEntity.ok(toMap(statusReviewService.create(sr)));
    }

    @PutMapping("/{reviewId}")
    public ResponseEntity<Map<String, Object>> update(
            @PathVariable String reviewId, @RequestBody Map<String, Object> body) {
        StatusReview updated = new StatusReview();
        if (body.get("overallStatus") != null) updated.setOverallStatus(StatusReview.TrafficLight.valueOf((String) body.get("overallStatus")));
        if (body.get("scheduleStatus") != null) updated.setScheduleStatus(StatusReview.TrafficLight.valueOf((String) body.get("scheduleStatus")));
        if (body.get("budgetStatus") != null) updated.setBudgetStatus(StatusReview.TrafficLight.valueOf((String) body.get("budgetStatus")));
        if (body.get("riskStatus") != null) updated.setRiskStatus(StatusReview.TrafficLight.valueOf((String) body.get("riskStatus")));
        if (body.get("summary") != null) updated.setSummary((String) body.get("summary"));
        if (body.get("achievements") != null) updated.setAchievements((String) body.get("achievements"));
        if (body.get("blockers") != null) updated.setBlockers((String) body.get("blockers"));
        if (body.get("nextActions") != null) updated.setNextActions((String) body.get("nextActions"));
        return ResponseEntity.ok(toMap(statusReviewService.update(reviewId, updated)));
    }

    @DeleteMapping("/{reviewId}")
    public ResponseEntity<Void> delete(@PathVariable String reviewId) {
        statusReviewService.delete(reviewId);
        return ResponseEntity.noContent().build();
    }

    private Map<String, Object> toMap(StatusReview sr) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id", sr.getId());
        m.put("projectId", sr.getProject().getId());
        m.put("reviewerId", sr.getReviewer().getId());
        m.put("reviewDate", sr.getReviewDate().toString());
        m.put("overallStatus", sr.getOverallStatus().name());
        m.put("scheduleStatus", sr.getScheduleStatus() != null ? sr.getScheduleStatus().name() : null);
        m.put("budgetStatus", sr.getBudgetStatus() != null ? sr.getBudgetStatus().name() : null);
        m.put("riskStatus", sr.getRiskStatus() != null ? sr.getRiskStatus().name() : null);
        m.put("summary", sr.getSummary());
        m.put("achievements", sr.getAchievements());
        m.put("blockers", sr.getBlockers());
        m.put("nextActions", sr.getNextActions());
        m.put("createdAt", sr.getCreatedAt() != null ? sr.getCreatedAt().toString() : null);
        return m;
    }
}
