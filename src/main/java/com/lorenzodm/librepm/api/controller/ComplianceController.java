package com.lorenzodm.librepm.api.controller;

import com.lorenzodm.librepm.api.exception.ResourceNotFoundException;
import com.lorenzodm.librepm.core.entity.*;
import com.lorenzodm.librepm.repository.*;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.*;

/**
 * Compliance REST controller (Phase 21 / PRD-21).
 * Manages data categories, retention policies, DSR requests, and compliance profiles.
 */
@RestController
@RequestMapping("/api/compliance")
public class ComplianceController {

    private final DataCategoryRepository categoryRepo;
    private final RetentionPolicyRepository retentionRepo;
    private final DsrRequestRepository dsrRepo;
    private final ComplianceProfileRepository profileRepo;

    public ComplianceController(DataCategoryRepository categoryRepo,
                                RetentionPolicyRepository retentionRepo,
                                DsrRequestRepository dsrRepo,
                                ComplianceProfileRepository profileRepo) {
        this.categoryRepo = categoryRepo;
        this.retentionRepo = retentionRepo;
        this.dsrRepo = dsrRepo;
        this.profileRepo = profileRepo;
    }

    // =========== DATA CATEGORIES ===========

    @GetMapping("/data-categories")
    public ResponseEntity<List<Map<String, Object>>> listCategories() {
        return ResponseEntity.ok(categoryRepo.findAllByOrderByNameAsc()
                .stream().map(this::categoryToMap).toList());
    }

    @PutMapping("/data-categories/{catId}")
    public ResponseEntity<Map<String, Object>> updateCategory(
            @PathVariable String catId, @RequestBody Map<String, Object> body) {
        DataCategory c = categoryRepo.findById(catId)
                .orElseThrow(() -> new ResourceNotFoundException("DataCategory not found: " + catId));
        if (body.containsKey("description")) c.setDescription((String) body.get("description"));
        if (body.containsKey("sensitivityLevel")) c.setSensitivityLevel((String) body.get("sensitivityLevel"));
        if (body.containsKey("defaultRetentionDays") && body.get("defaultRetentionDays") != null)
            c.setDefaultRetentionDays(((Number) body.get("defaultRetentionDays")).intValue());
        if (body.containsKey("processingPurposes")) c.setProcessingPurposes((String) body.get("processingPurposes"));
        return ResponseEntity.ok(categoryToMap(categoryRepo.save(c)));
    }

    // =========== RETENTION POLICIES ===========

    @GetMapping("/retention-policies")
    public ResponseEntity<List<Map<String, Object>>> listRetentionPolicies() {
        return ResponseEntity.ok(retentionRepo.findByIsActiveTrue()
                .stream().map(this::retentionToMap).toList());
    }

    @PostMapping("/retention-policies")
    public ResponseEntity<Map<String, Object>> createRetentionPolicy(@RequestBody Map<String, Object> body) {
        RetentionPolicy rp = new RetentionPolicy();
        rp.setDataCategoryId((String) body.get("dataCategoryId"));
        rp.setScope((String) body.getOrDefault("scope", "WORKSPACE"));
        rp.setScopeEntityId((String) body.get("scopeEntityId"));
        rp.setRetentionDays(((Number) body.get("retentionDays")).intValue());
        rp.setActionOnExpiry((String) body.getOrDefault("actionOnExpiry", "ARCHIVE"));
        rp.setIsActive(true);
        return ResponseEntity.ok(retentionToMap(retentionRepo.save(rp)));
    }

    @PutMapping("/retention-policies/{rpId}")
    public ResponseEntity<Map<String, Object>> updateRetentionPolicy(
            @PathVariable String rpId, @RequestBody Map<String, Object> body) {
        RetentionPolicy rp = retentionRepo.findById(rpId)
                .orElseThrow(() -> new ResourceNotFoundException("RetentionPolicy not found: " + rpId));
        if (body.containsKey("retentionDays") && body.get("retentionDays") != null)
            rp.setRetentionDays(((Number) body.get("retentionDays")).intValue());
        if (body.containsKey("actionOnExpiry")) rp.setActionOnExpiry((String) body.get("actionOnExpiry"));
        if (body.containsKey("isActive")) rp.setIsActive((Boolean) body.get("isActive"));
        return ResponseEntity.ok(retentionToMap(retentionRepo.save(rp)));
    }

    @DeleteMapping("/retention-policies/{rpId}")
    public ResponseEntity<Void> deleteRetentionPolicy(@PathVariable String rpId) {
        retentionRepo.deleteById(rpId);
        return ResponseEntity.noContent().build();
    }

    // =========== DSR REQUESTS ===========

    @GetMapping("/dsr-requests")
    public ResponseEntity<List<Map<String, Object>>> listDsrRequests(
            @RequestParam(required = false) String userId) {
        List<DsrRequest> list = userId != null
                ? dsrRepo.findByUserIdOrderByRequestedAtDesc(userId)
                : dsrRepo.findAll();
        return ResponseEntity.ok(list.stream().map(this::dsrToMap).toList());
    }

    @PostMapping("/dsr-requests")
    public ResponseEntity<Map<String, Object>> createDsrRequest(@RequestBody Map<String, Object> body) {
        DsrRequest dsr = new DsrRequest();
        dsr.setUserId((String) body.get("userId"));
        dsr.setRequestType((String) body.get("requestType"));
        dsr.setStatus("PENDING");
        dsr.setDescription((String) body.get("description"));
        return ResponseEntity.ok(dsrToMap(dsrRepo.save(dsr)));
    }

    @PutMapping("/dsr-requests/{dsrId}")
    public ResponseEntity<Map<String, Object>> updateDsrRequest(
            @PathVariable String dsrId, @RequestBody Map<String, Object> body) {
        DsrRequest dsr = dsrRepo.findById(dsrId)
                .orElseThrow(() -> new ResourceNotFoundException("DsrRequest not found: " + dsrId));
        if (body.containsKey("status")) {
            String newStatus = (String) body.get("status");
            dsr.setStatus(newStatus);
            if ("COMPLETED".equals(newStatus) && dsr.getCompletedAt() == null) {
                dsr.setCompletedAt(Instant.now());
            }
        }
        if (body.containsKey("resultNotes")) dsr.setResultNotes((String) body.get("resultNotes"));
        return ResponseEntity.ok(dsrToMap(dsrRepo.save(dsr)));
    }

    // =========== COMPLIANCE PROFILES ===========

    @GetMapping("/profiles")
    public ResponseEntity<List<Map<String, Object>>> listProfiles() {
        return ResponseEntity.ok(profileRepo.findAllByOrderByNameAsc()
                .stream().map(this::profileToMap).toList());
    }

    @PostMapping("/profiles")
    public ResponseEntity<Map<String, Object>> createProfile(@RequestBody Map<String, Object> body) {
        ComplianceProfile cp = new ComplianceProfile();
        cp.setName((String) body.getOrDefault("name", "Custom"));
        cp.setDescription((String) body.get("description"));
        cp.setSettingsJson((String) body.getOrDefault("settingsJson", "{}"));
        cp.setIsSystem(false);
        return ResponseEntity.ok(profileToMap(profileRepo.save(cp)));
    }

    @PutMapping("/profiles/{profileId}")
    public ResponseEntity<Map<String, Object>> updateProfile(
            @PathVariable String profileId, @RequestBody Map<String, Object> body) {
        ComplianceProfile cp = profileRepo.findById(profileId)
                .orElseThrow(() -> new ResourceNotFoundException("ComplianceProfile not found: " + profileId));
        if (body.containsKey("name")) cp.setName((String) body.get("name"));
        if (body.containsKey("description")) cp.setDescription((String) body.get("description"));
        if (body.containsKey("settingsJson")) cp.setSettingsJson((String) body.get("settingsJson"));
        return ResponseEntity.ok(profileToMap(profileRepo.save(cp)));
    }

    // =========== MAPPERS ===========

    private Map<String, Object> categoryToMap(DataCategory c) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id", c.getId());
        m.put("name", c.getName());
        m.put("description", c.getDescription());
        m.put("sensitivityLevel", c.getSensitivityLevel());
        m.put("defaultRetentionDays", c.getDefaultRetentionDays());
        m.put("processingPurposes", c.getProcessingPurposes());
        return m;
    }

    private Map<String, Object> retentionToMap(RetentionPolicy rp) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id", rp.getId());
        m.put("dataCategoryId", rp.getDataCategoryId());
        m.put("scope", rp.getScope());
        m.put("scopeEntityId", rp.getScopeEntityId());
        m.put("retentionDays", rp.getRetentionDays());
        m.put("actionOnExpiry", rp.getActionOnExpiry());
        m.put("isActive", rp.getIsActive());
        return m;
    }

    private Map<String, Object> dsrToMap(DsrRequest dsr) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id", dsr.getId());
        m.put("userId", dsr.getUserId());
        m.put("requestType", dsr.getRequestType());
        m.put("status", dsr.getStatus());
        m.put("description", dsr.getDescription());
        m.put("requestedAt", dsr.getRequestedAt());
        m.put("completedAt", dsr.getCompletedAt());
        m.put("resultNotes", dsr.getResultNotes());
        return m;
    }

    private Map<String, Object> profileToMap(ComplianceProfile cp) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id", cp.getId());
        m.put("name", cp.getName());
        m.put("description", cp.getDescription());
        m.put("settingsJson", cp.getSettingsJson());
        m.put("isSystem", cp.getIsSystem());
        return m;
    }
}
