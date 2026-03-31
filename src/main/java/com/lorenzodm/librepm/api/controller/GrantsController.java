package com.lorenzodm.librepm.api.controller;

import com.lorenzodm.librepm.api.exception.ResourceNotFoundException;
import com.lorenzodm.librepm.core.entity.*;
import com.lorenzodm.librepm.repository.*;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.time.LocalDate;
import java.util.*;

/**
 * Grants REST controller (Phase 19 / PRD-19).
 * Manages grant calls, requirements, submissions, obligations, and reporting periods.
 */
@RestController
@RequestMapping("/api/projects/{projectId}/grants")
public class GrantsController {

    private final GrantCallRepository callRepo;
    private final CallRequirementRepository reqRepo;
    private final SubmissionPackageRepository subRepo;
    private final ObligationRecordRepository oblRepo;
    private final ReportingPeriodRepository rpRepo;

    public GrantsController(GrantCallRepository callRepo,
                            CallRequirementRepository reqRepo,
                            SubmissionPackageRepository subRepo,
                            ObligationRecordRepository oblRepo,
                            ReportingPeriodRepository rpRepo) {
        this.callRepo = callRepo;
        this.reqRepo = reqRepo;
        this.subRepo = subRepo;
        this.oblRepo = oblRepo;
        this.rpRepo = rpRepo;
    }

    // =========== GRANT CALLS ===========

    @GetMapping("/calls")
    public ResponseEntity<List<Map<String, Object>>> listCalls(@PathVariable String projectId) {
        return ResponseEntity.ok(callRepo.findByProjectIdOrderByDeadlineAsc(projectId)
                .stream().map(this::callToMap).toList());
    }

    @PostMapping("/calls")
    public ResponseEntity<Map<String, Object>> createCall(
            @PathVariable String projectId, @RequestBody Map<String, Object> body) {
        GrantCall c = new GrantCall();
        c.setProjectId(projectId);
        c.setTitle((String) body.getOrDefault("title", "New Call"));
        c.setDescription((String) body.get("description"));
        c.setIssuer((String) body.get("issuer"));
        c.setReferenceCode((String) body.get("referenceCode"));
        c.setStatus((String) body.getOrDefault("status", "IDENTIFIED"));
        if (body.get("deadline") != null) c.setDeadline(LocalDate.parse((String) body.get("deadline")));
        if (body.get("budgetAvailable") != null) c.setBudgetAvailable(((Number) body.get("budgetAvailable")).doubleValue());
        c.setCurrency((String) body.getOrDefault("currency", "EUR"));
        c.setUrl((String) body.get("url"));
        c.setNotes((String) body.get("notes"));
        return ResponseEntity.ok(callToMap(callRepo.save(c)));
    }

    @PutMapping("/calls/{callId}")
    public ResponseEntity<Map<String, Object>> updateCall(
            @PathVariable String projectId, @PathVariable String callId,
            @RequestBody Map<String, Object> body) {
        GrantCall c = callRepo.findById(callId).orElseThrow(() -> new ResourceNotFoundException("GrantCall not found: " + callId));
        if (body.containsKey("title")) c.setTitle((String) body.get("title"));
        if (body.containsKey("description")) c.setDescription((String) body.get("description"));
        if (body.containsKey("issuer")) c.setIssuer((String) body.get("issuer"));
        if (body.containsKey("referenceCode")) c.setReferenceCode((String) body.get("referenceCode"));
        if (body.containsKey("status")) c.setStatus((String) body.get("status"));
        if (body.containsKey("deadline")) c.setDeadline(body.get("deadline") != null ? LocalDate.parse((String) body.get("deadline")) : null);
        if (body.containsKey("budgetAvailable") && body.get("budgetAvailable") != null) c.setBudgetAvailable(((Number) body.get("budgetAvailable")).doubleValue());
        if (body.containsKey("currency")) c.setCurrency((String) body.get("currency"));
        if (body.containsKey("url")) c.setUrl((String) body.get("url"));
        if (body.containsKey("notes")) c.setNotes((String) body.get("notes"));
        c.setUpdatedAt(Instant.now());
        return ResponseEntity.ok(callToMap(callRepo.save(c)));
    }

    @DeleteMapping("/calls/{callId}")
    public ResponseEntity<Void> deleteCall(@PathVariable String projectId, @PathVariable String callId) {
        callRepo.deleteById(callId);
        return ResponseEntity.noContent().build();
    }

    // =========== CALL REQUIREMENTS ===========

    @GetMapping("/calls/{callId}/requirements")
    public ResponseEntity<List<Map<String, Object>>> listRequirements(@PathVariable String callId) {
        return ResponseEntity.ok(reqRepo.findByCallIdOrderBySortOrderAsc(callId)
                .stream().map(this::reqToMap).toList());
    }

    @PostMapping("/calls/{callId}/requirements")
    public ResponseEntity<Map<String, Object>> createRequirement(
            @PathVariable String callId, @RequestBody Map<String, Object> body) {
        CallRequirement r = new CallRequirement();
        r.setCallId(callId);
        r.setDescription((String) body.getOrDefault("description", ""));
        r.setRequirementType((String) body.get("requirementType"));
        r.setIsMet(body.get("isMet") != null && (Boolean) body.get("isMet"));
        r.setEvidenceNote((String) body.get("evidenceNote"));
        if (body.get("sortOrder") != null) r.setSortOrder(((Number) body.get("sortOrder")).intValue());
        return ResponseEntity.ok(reqToMap(reqRepo.save(r)));
    }

    @PutMapping("/calls/{callId}/requirements/{reqId}")
    public ResponseEntity<Map<String, Object>> updateRequirement(
            @PathVariable String callId, @PathVariable String reqId,
            @RequestBody Map<String, Object> body) {
        CallRequirement r = reqRepo.findById(reqId).orElseThrow(() -> new ResourceNotFoundException("CallRequirement not found: " + reqId));
        if (body.containsKey("description")) r.setDescription((String) body.get("description"));
        if (body.containsKey("requirementType")) r.setRequirementType((String) body.get("requirementType"));
        if (body.containsKey("isMet")) r.setIsMet((Boolean) body.get("isMet"));
        if (body.containsKey("evidenceNote")) r.setEvidenceNote((String) body.get("evidenceNote"));
        if (body.containsKey("sortOrder")) r.setSortOrder(((Number) body.get("sortOrder")).intValue());
        return ResponseEntity.ok(reqToMap(reqRepo.save(r)));
    }

    @DeleteMapping("/calls/{callId}/requirements/{reqId}")
    public ResponseEntity<Void> deleteRequirement(@PathVariable String callId, @PathVariable String reqId) {
        reqRepo.deleteById(reqId);
        return ResponseEntity.noContent().build();
    }

    // =========== SUBMISSION PACKAGES ===========

    @GetMapping("/calls/{callId}/submissions")
    public ResponseEntity<List<Map<String, Object>>> listSubmissions(@PathVariable String callId) {
        return ResponseEntity.ok(subRepo.findByCallIdOrderByCreatedAtDesc(callId)
                .stream().map(this::subToMap).toList());
    }

    @PostMapping("/calls/{callId}/submissions")
    public ResponseEntity<Map<String, Object>> createSubmission(
            @PathVariable String callId, @RequestBody Map<String, Object> body) {
        SubmissionPackage s = new SubmissionPackage();
        s.setCallId(callId);
        s.setStatus((String) body.getOrDefault("status", "DRAFT"));
        s.setSubmittedBy((String) body.get("submittedBy"));
        s.setNotes((String) body.get("notes"));
        return ResponseEntity.ok(subToMap(subRepo.save(s)));
    }

    @PutMapping("/calls/{callId}/submissions/{subId}")
    public ResponseEntity<Map<String, Object>> updateSubmission(
            @PathVariable String callId, @PathVariable String subId,
            @RequestBody Map<String, Object> body) {
        SubmissionPackage s = subRepo.findById(subId).orElseThrow(() -> new ResourceNotFoundException("SubmissionPackage not found: " + subId));
        if (body.containsKey("status")) {
            String newStatus = (String) body.get("status");
            s.setStatus(newStatus);
            if ("SUBMITTED".equals(newStatus) && s.getSubmittedAt() == null) {
                s.setSubmittedAt(Instant.now());
            }
        }
        if (body.containsKey("submittedBy")) s.setSubmittedBy((String) body.get("submittedBy"));
        if (body.containsKey("notes")) s.setNotes((String) body.get("notes"));
        return ResponseEntity.ok(subToMap(subRepo.save(s)));
    }

    // =========== OBLIGATIONS ===========

    @GetMapping("/obligations")
    public ResponseEntity<List<Map<String, Object>>> listObligations(@PathVariable String projectId) {
        return ResponseEntity.ok(oblRepo.findByProjectIdOrderByDeadlineAsc(projectId)
                .stream().map(this::oblToMap).toList());
    }

    @PostMapping("/obligations")
    public ResponseEntity<Map<String, Object>> createObligation(
            @PathVariable String projectId, @RequestBody Map<String, Object> body) {
        ObligationRecord o = new ObligationRecord();
        o.setProjectId(projectId);
        o.setTitle((String) body.getOrDefault("title", "New Obligation"));
        o.setDescription((String) body.get("description"));
        o.setType((String) body.get("type"));
        if (body.get("deadline") != null) o.setDeadline(LocalDate.parse((String) body.get("deadline")));
        o.setResponsibleUserId((String) body.get("responsibleUserId"));
        o.setStatus((String) body.getOrDefault("status", "PENDING"));
        o.setLinkedDeliverableId((String) body.get("linkedDeliverableId"));
        o.setLinkedBudgetLineId((String) body.get("linkedBudgetLineId"));
        o.setNotes((String) body.get("notes"));
        return ResponseEntity.ok(oblToMap(oblRepo.save(o)));
    }

    @PutMapping("/obligations/{oblId}")
    public ResponseEntity<Map<String, Object>> updateObligation(
            @PathVariable String projectId, @PathVariable String oblId,
            @RequestBody Map<String, Object> body) {
        ObligationRecord o = oblRepo.findById(oblId).orElseThrow(() -> new ResourceNotFoundException("ObligationRecord not found: " + oblId));
        if (body.containsKey("title")) o.setTitle((String) body.get("title"));
        if (body.containsKey("description")) o.setDescription((String) body.get("description"));
        if (body.containsKey("type")) o.setType((String) body.get("type"));
        if (body.containsKey("deadline")) o.setDeadline(body.get("deadline") != null ? LocalDate.parse((String) body.get("deadline")) : null);
        if (body.containsKey("responsibleUserId")) o.setResponsibleUserId((String) body.get("responsibleUserId"));
        if (body.containsKey("status")) o.setStatus((String) body.get("status"));
        if (body.containsKey("linkedDeliverableId")) o.setLinkedDeliverableId((String) body.get("linkedDeliverableId"));
        if (body.containsKey("linkedBudgetLineId")) o.setLinkedBudgetLineId((String) body.get("linkedBudgetLineId"));
        if (body.containsKey("notes")) o.setNotes((String) body.get("notes"));
        return ResponseEntity.ok(oblToMap(oblRepo.save(o)));
    }

    @DeleteMapping("/obligations/{oblId}")
    public ResponseEntity<Void> deleteObligation(@PathVariable String projectId, @PathVariable String oblId) {
        oblRepo.deleteById(oblId);
        return ResponseEntity.noContent().build();
    }

    // =========== REPORTING PERIODS ===========

    @GetMapping("/reporting-periods")
    public ResponseEntity<List<Map<String, Object>>> listReportingPeriods(@PathVariable String projectId) {
        return ResponseEntity.ok(rpRepo.findByProjectIdOrderByDueDateAsc(projectId)
                .stream().map(this::rpToMap).toList());
    }

    @PostMapping("/reporting-periods")
    public ResponseEntity<Map<String, Object>> createReportingPeriod(
            @PathVariable String projectId, @RequestBody Map<String, Object> body) {
        ReportingPeriod rp = new ReportingPeriod();
        rp.setProjectId(projectId);
        rp.setName((String) body.getOrDefault("name", "Period"));
        rp.setPeriodStart(LocalDate.parse((String) body.get("periodStart")));
        rp.setPeriodEnd(LocalDate.parse((String) body.get("periodEnd")));
        if (body.get("dueDate") != null) rp.setDueDate(LocalDate.parse((String) body.get("dueDate")));
        rp.setStatus((String) body.getOrDefault("status", "UPCOMING"));
        rp.setNotes((String) body.get("notes"));
        return ResponseEntity.ok(rpToMap(rpRepo.save(rp)));
    }

    @PutMapping("/reporting-periods/{rpId}")
    public ResponseEntity<Map<String, Object>> updateReportingPeriod(
            @PathVariable String projectId, @PathVariable String rpId,
            @RequestBody Map<String, Object> body) {
        ReportingPeriod rp = rpRepo.findById(rpId).orElseThrow(() -> new ResourceNotFoundException("ReportingPeriod not found: " + rpId));
        if (body.containsKey("name")) rp.setName((String) body.get("name"));
        if (body.containsKey("periodStart")) rp.setPeriodStart(LocalDate.parse((String) body.get("periodStart")));
        if (body.containsKey("periodEnd")) rp.setPeriodEnd(LocalDate.parse((String) body.get("periodEnd")));
        if (body.containsKey("dueDate")) rp.setDueDate(body.get("dueDate") != null ? LocalDate.parse((String) body.get("dueDate")) : null);
        if (body.containsKey("status")) rp.setStatus((String) body.get("status"));
        if (body.containsKey("submissionAssetId")) rp.setSubmissionAssetId((String) body.get("submissionAssetId"));
        if (body.containsKey("notes")) rp.setNotes((String) body.get("notes"));
        return ResponseEntity.ok(rpToMap(rpRepo.save(rp)));
    }

    @DeleteMapping("/reporting-periods/{rpId}")
    public ResponseEntity<Void> deleteReportingPeriod(@PathVariable String projectId, @PathVariable String rpId) {
        rpRepo.deleteById(rpId);
        return ResponseEntity.noContent().build();
    }

    // =========== MAPPERS ===========

    private Map<String, Object> callToMap(GrantCall c) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id", c.getId());
        m.put("projectId", c.getProjectId());
        m.put("title", c.getTitle());
        m.put("description", c.getDescription());
        m.put("issuer", c.getIssuer());
        m.put("referenceCode", c.getReferenceCode());
        m.put("status", c.getStatus());
        m.put("deadline", c.getDeadline());
        m.put("budgetAvailable", c.getBudgetAvailable());
        m.put("currency", c.getCurrency());
        m.put("url", c.getUrl());
        m.put("notes", c.getNotes());
        m.put("createdAt", c.getCreatedAt());
        m.put("updatedAt", c.getUpdatedAt());
        return m;
    }

    private Map<String, Object> reqToMap(CallRequirement r) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id", r.getId());
        m.put("callId", r.getCallId());
        m.put("description", r.getDescription());
        m.put("requirementType", r.getRequirementType());
        m.put("isMet", r.getIsMet());
        m.put("evidenceNote", r.getEvidenceNote());
        m.put("sortOrder", r.getSortOrder());
        return m;
    }

    private Map<String, Object> subToMap(SubmissionPackage s) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id", s.getId());
        m.put("callId", s.getCallId());
        m.put("status", s.getStatus());
        m.put("submittedAt", s.getSubmittedAt());
        m.put("submittedBy", s.getSubmittedBy());
        m.put("notes", s.getNotes());
        m.put("createdAt", s.getCreatedAt());
        return m;
    }

    private Map<String, Object> oblToMap(ObligationRecord o) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id", o.getId());
        m.put("projectId", o.getProjectId());
        m.put("title", o.getTitle());
        m.put("description", o.getDescription());
        m.put("type", o.getType());
        m.put("deadline", o.getDeadline());
        m.put("responsibleUserId", o.getResponsibleUserId());
        m.put("status", o.getStatus());
        m.put("linkedDeliverableId", o.getLinkedDeliverableId());
        m.put("linkedBudgetLineId", o.getLinkedBudgetLineId());
        m.put("notes", o.getNotes());
        m.put("createdAt", o.getCreatedAt());
        return m;
    }

    private Map<String, Object> rpToMap(ReportingPeriod rp) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id", rp.getId());
        m.put("projectId", rp.getProjectId());
        m.put("name", rp.getName());
        m.put("periodStart", rp.getPeriodStart());
        m.put("periodEnd", rp.getPeriodEnd());
        m.put("dueDate", rp.getDueDate());
        m.put("status", rp.getStatus());
        m.put("submissionAssetId", rp.getSubmissionAssetId());
        m.put("notes", rp.getNotes());
        return m;
    }
}
