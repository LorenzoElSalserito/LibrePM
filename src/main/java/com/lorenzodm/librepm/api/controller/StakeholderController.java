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
 * Stakeholder REST controller (Phase 19 / PRD-19).
 * Manages stakeholders, sponsors, commitments, donors, donations, and partners.
 */
@RestController
@RequestMapping("/api/projects/{projectId}/stakeholders")
public class StakeholderController {

    private final StakeholderRepository stakeholderRepo;
    private final SponsorRepository sponsorRepo;
    private final SponsorCommitmentRepository commitRepo;
    private final DonorRepository donorRepo;
    private final DonationRepository donationRepo;
    private final PartnerOrganisationRepository partnerRepo;

    public StakeholderController(StakeholderRepository stakeholderRepo,
                                 SponsorRepository sponsorRepo,
                                 SponsorCommitmentRepository commitRepo,
                                 DonorRepository donorRepo,
                                 DonationRepository donationRepo,
                                 PartnerOrganisationRepository partnerRepo) {
        this.stakeholderRepo = stakeholderRepo;
        this.sponsorRepo = sponsorRepo;
        this.commitRepo = commitRepo;
        this.donorRepo = donorRepo;
        this.donationRepo = donationRepo;
        this.partnerRepo = partnerRepo;
    }

    // =========== STAKEHOLDERS ===========

    @GetMapping
    public ResponseEntity<List<Map<String, Object>>> listStakeholders(@PathVariable String projectId) {
        return ResponseEntity.ok(stakeholderRepo.findByProjectIdOrderByNameAsc(projectId)
                .stream().map(this::stakeholderToMap).toList());
    }

    @PostMapping
    public ResponseEntity<Map<String, Object>> createStakeholder(
            @PathVariable String projectId, @RequestBody Map<String, Object> body) {
        Stakeholder s = new Stakeholder();
        s.setProjectId(projectId);
        s.setName((String) body.getOrDefault("name", ""));
        s.setOrganization((String) body.get("organization"));
        s.setRoleDescription((String) body.get("roleDescription"));
        s.setInfluenceLevel((String) body.getOrDefault("influenceLevel", "MEDIUM"));
        s.setInterestLevel((String) body.getOrDefault("interestLevel", "MEDIUM"));
        s.setEngagementStrategy((String) body.get("engagementStrategy"));
        s.setChannel((String) body.get("channel"));
        s.setNotes((String) body.get("notes"));
        return ResponseEntity.ok(stakeholderToMap(stakeholderRepo.save(s)));
    }

    @PutMapping("/{stakeholderId}")
    public ResponseEntity<Map<String, Object>> updateStakeholder(
            @PathVariable String projectId, @PathVariable String stakeholderId,
            @RequestBody Map<String, Object> body) {
        Stakeholder s = stakeholderRepo.findById(stakeholderId)
                .orElseThrow(() -> new ResourceNotFoundException("Stakeholder not found: " + stakeholderId));
        if (body.containsKey("name")) s.setName((String) body.get("name"));
        if (body.containsKey("organization")) s.setOrganization((String) body.get("organization"));
        if (body.containsKey("roleDescription")) s.setRoleDescription((String) body.get("roleDescription"));
        if (body.containsKey("influenceLevel")) s.setInfluenceLevel((String) body.get("influenceLevel"));
        if (body.containsKey("interestLevel")) s.setInterestLevel((String) body.get("interestLevel"));
        if (body.containsKey("engagementStrategy")) s.setEngagementStrategy((String) body.get("engagementStrategy"));
        if (body.containsKey("channel")) s.setChannel((String) body.get("channel"));
        if (body.containsKey("notes")) s.setNotes((String) body.get("notes"));
        return ResponseEntity.ok(stakeholderToMap(stakeholderRepo.save(s)));
    }

    @DeleteMapping("/{stakeholderId}")
    public ResponseEntity<Void> deleteStakeholder(@PathVariable String projectId, @PathVariable String stakeholderId) {
        stakeholderRepo.deleteById(stakeholderId);
        return ResponseEntity.noContent().build();
    }

    // =========== SPONSORS (global, not project-scoped) ===========

    @GetMapping("/sponsors")
    public ResponseEntity<List<Map<String, Object>>> listSponsors(@PathVariable String projectId) {
        return ResponseEntity.ok(sponsorRepo.findAllByOrderByNameAsc()
                .stream().map(this::sponsorToMap).toList());
    }

    @PostMapping("/sponsors")
    public ResponseEntity<Map<String, Object>> createSponsor(
            @PathVariable String projectId, @RequestBody Map<String, Object> body) {
        Sponsor s = new Sponsor();
        s.setName((String) body.getOrDefault("name", ""));
        s.setOrganization((String) body.get("organization"));
        s.setEmail((String) body.get("email"));
        s.setContactPerson((String) body.get("contactPerson"));
        s.setType((String) body.getOrDefault("type", "CORPORATE"));
        s.setNotes((String) body.get("notes"));
        return ResponseEntity.ok(sponsorToMap(sponsorRepo.save(s)));
    }

    @PutMapping("/sponsors/{sponsorId}")
    public ResponseEntity<Map<String, Object>> updateSponsor(
            @PathVariable String projectId, @PathVariable String sponsorId,
            @RequestBody Map<String, Object> body) {
        Sponsor s = sponsorRepo.findById(sponsorId)
                .orElseThrow(() -> new ResourceNotFoundException("Sponsor not found: " + sponsorId));
        if (body.containsKey("name")) s.setName((String) body.get("name"));
        if (body.containsKey("organization")) s.setOrganization((String) body.get("organization"));
        if (body.containsKey("email")) s.setEmail((String) body.get("email"));
        if (body.containsKey("contactPerson")) s.setContactPerson((String) body.get("contactPerson"));
        if (body.containsKey("type")) s.setType((String) body.get("type"));
        if (body.containsKey("notes")) s.setNotes((String) body.get("notes"));
        return ResponseEntity.ok(sponsorToMap(sponsorRepo.save(s)));
    }

    @DeleteMapping("/sponsors/{sponsorId}")
    public ResponseEntity<Void> deleteSponsor(@PathVariable String projectId, @PathVariable String sponsorId) {
        sponsorRepo.deleteById(sponsorId);
        return ResponseEntity.noContent().build();
    }

    // =========== SPONSOR COMMITMENTS ===========

    @GetMapping("/commitments")
    public ResponseEntity<List<Map<String, Object>>> listCommitments(@PathVariable String projectId) {
        return ResponseEntity.ok(commitRepo.findByProjectIdOrderBySponsorIdAsc(projectId)
                .stream().map(this::commitToMap).toList());
    }

    @PostMapping("/commitments")
    public ResponseEntity<Map<String, Object>> createCommitment(
            @PathVariable String projectId, @RequestBody Map<String, Object> body) {
        SponsorCommitment c = new SponsorCommitment();
        c.setSponsorId((String) body.get("sponsorId"));
        c.setProjectId(projectId);
        c.setDescription((String) body.get("description"));
        if (body.get("committedAmount") != null) c.setCommittedAmount(((Number) body.get("committedAmount")).doubleValue());
        c.setCurrency((String) body.getOrDefault("currency", "EUR"));
        c.setStatus((String) body.getOrDefault("status", "PROPOSED"));
        if (body.get("agreementDate") != null) c.setAgreementDate(LocalDate.parse((String) body.get("agreementDate")));
        return ResponseEntity.ok(commitToMap(commitRepo.save(c)));
    }

    @PutMapping("/commitments/{commitId}")
    public ResponseEntity<Map<String, Object>> updateCommitment(
            @PathVariable String projectId, @PathVariable String commitId,
            @RequestBody Map<String, Object> body) {
        SponsorCommitment c = commitRepo.findById(commitId)
                .orElseThrow(() -> new ResourceNotFoundException("SponsorCommitment not found: " + commitId));
        if (body.containsKey("description")) c.setDescription((String) body.get("description"));
        if (body.containsKey("committedAmount") && body.get("committedAmount") != null) c.setCommittedAmount(((Number) body.get("committedAmount")).doubleValue());
        if (body.containsKey("currency")) c.setCurrency((String) body.get("currency"));
        if (body.containsKey("status")) c.setStatus((String) body.get("status"));
        if (body.containsKey("agreementDate")) c.setAgreementDate(body.get("agreementDate") != null ? LocalDate.parse((String) body.get("agreementDate")) : null);
        if (body.containsKey("agreementAssetId")) c.setAgreementAssetId((String) body.get("agreementAssetId"));
        return ResponseEntity.ok(commitToMap(commitRepo.save(c)));
    }

    @DeleteMapping("/commitments/{commitId}")
    public ResponseEntity<Void> deleteCommitment(@PathVariable String projectId, @PathVariable String commitId) {
        commitRepo.deleteById(commitId);
        return ResponseEntity.noContent().build();
    }

    // =========== DONORS (global) ===========

    @GetMapping("/donors")
    public ResponseEntity<List<Map<String, Object>>> listDonors(@PathVariable String projectId) {
        return ResponseEntity.ok(donorRepo.findAllByOrderByNameAsc()
                .stream().map(this::donorToMap).toList());
    }

    @PostMapping("/donors")
    public ResponseEntity<Map<String, Object>> createDonor(
            @PathVariable String projectId, @RequestBody Map<String, Object> body) {
        Donor d = new Donor();
        d.setName((String) body.getOrDefault("name", ""));
        d.setOrganization((String) body.get("organization"));
        d.setEmail((String) body.get("email"));
        d.setPhone((String) body.get("phone"));
        d.setType((String) body.getOrDefault("type", "INDIVIDUAL"));
        d.setNotes((String) body.get("notes"));
        return ResponseEntity.ok(donorToMap(donorRepo.save(d)));
    }

    @PutMapping("/donors/{donorId}")
    public ResponseEntity<Map<String, Object>> updateDonor(
            @PathVariable String projectId, @PathVariable String donorId,
            @RequestBody Map<String, Object> body) {
        Donor d = donorRepo.findById(donorId)
                .orElseThrow(() -> new ResourceNotFoundException("Donor not found: " + donorId));
        if (body.containsKey("name")) d.setName((String) body.get("name"));
        if (body.containsKey("organization")) d.setOrganization((String) body.get("organization"));
        if (body.containsKey("email")) d.setEmail((String) body.get("email"));
        if (body.containsKey("phone")) d.setPhone((String) body.get("phone"));
        if (body.containsKey("type")) d.setType((String) body.get("type"));
        if (body.containsKey("notes")) d.setNotes((String) body.get("notes"));
        return ResponseEntity.ok(donorToMap(donorRepo.save(d)));
    }

    @DeleteMapping("/donors/{donorId}")
    public ResponseEntity<Void> deleteDonor(@PathVariable String projectId, @PathVariable String donorId) {
        donorRepo.deleteById(donorId);
        return ResponseEntity.noContent().build();
    }

    // =========== DONATIONS ===========

    @GetMapping("/donations")
    public ResponseEntity<List<Map<String, Object>>> listDonations(@PathVariable String projectId) {
        return ResponseEntity.ok(donationRepo.findByProjectIdOrderByDonationDateDesc(projectId)
                .stream().map(this::donationToMap).toList());
    }

    @PostMapping("/donations")
    public ResponseEntity<Map<String, Object>> createDonation(
            @PathVariable String projectId, @RequestBody Map<String, Object> body) {
        Donation d = new Donation();
        d.setDonorId((String) body.get("donorId"));
        d.setProjectId(projectId);
        d.setAmount(((Number) body.get("amount")).doubleValue());
        d.setCurrency((String) body.getOrDefault("currency", "EUR"));
        if (body.get("donationDate") != null) d.setDonationDate(LocalDate.parse((String) body.get("donationDate")));
        d.setIsRestricted(body.get("isRestricted") != null && (Boolean) body.get("isRestricted"));
        d.setRestrictionDescription((String) body.get("restrictionDescription"));
        d.setReceiptAssetId((String) body.get("receiptAssetId"));
        d.setNotes((String) body.get("notes"));
        return ResponseEntity.ok(donationToMap(donationRepo.save(d)));
    }

    @PutMapping("/donations/{donationId}")
    public ResponseEntity<Map<String, Object>> updateDonation(
            @PathVariable String projectId, @PathVariable String donationId,
            @RequestBody Map<String, Object> body) {
        Donation d = donationRepo.findById(donationId)
                .orElseThrow(() -> new ResourceNotFoundException("Donation not found: " + donationId));
        if (body.containsKey("amount") && body.get("amount") != null) d.setAmount(((Number) body.get("amount")).doubleValue());
        if (body.containsKey("currency")) d.setCurrency((String) body.get("currency"));
        if (body.containsKey("donationDate")) d.setDonationDate(body.get("donationDate") != null ? LocalDate.parse((String) body.get("donationDate")) : null);
        if (body.containsKey("isRestricted")) d.setIsRestricted(body.get("isRestricted") != null && (Boolean) body.get("isRestricted"));
        if (body.containsKey("restrictionDescription")) d.setRestrictionDescription((String) body.get("restrictionDescription"));
        if (body.containsKey("receiptAssetId")) d.setReceiptAssetId((String) body.get("receiptAssetId"));
        if (body.containsKey("notes")) d.setNotes((String) body.get("notes"));
        return ResponseEntity.ok(donationToMap(donationRepo.save(d)));
    }

    @DeleteMapping("/donations/{donationId}")
    public ResponseEntity<Void> deleteDonation(@PathVariable String projectId, @PathVariable String donationId) {
        donationRepo.deleteById(donationId);
        return ResponseEntity.noContent().build();
    }

    // =========== PARTNERS ===========

    @GetMapping("/partners")
    public ResponseEntity<List<Map<String, Object>>> listPartners(@PathVariable String projectId) {
        return ResponseEntity.ok(partnerRepo.findByProjectIdOrderByNameAsc(projectId)
                .stream().map(this::partnerToMap).toList());
    }

    @PostMapping("/partners")
    public ResponseEntity<Map<String, Object>> createPartner(
            @PathVariable String projectId, @RequestBody Map<String, Object> body) {
        PartnerOrganisation p = new PartnerOrganisation();
        p.setProjectId(projectId);
        p.setName((String) body.getOrDefault("name", ""));
        p.setCountry((String) body.get("country"));
        p.setRoleInProject((String) body.getOrDefault("roleInProject", "PARTNER"));
        p.setContactPerson((String) body.get("contactPerson"));
        p.setContactEmail((String) body.get("contactEmail"));
        if (body.get("budgetShare") != null) p.setBudgetShare(((Number) body.get("budgetShare")).doubleValue());
        p.setNotes((String) body.get("notes"));
        return ResponseEntity.ok(partnerToMap(partnerRepo.save(p)));
    }

    @PutMapping("/partners/{partnerId}")
    public ResponseEntity<Map<String, Object>> updatePartner(
            @PathVariable String projectId, @PathVariable String partnerId,
            @RequestBody Map<String, Object> body) {
        PartnerOrganisation p = partnerRepo.findById(partnerId)
                .orElseThrow(() -> new ResourceNotFoundException("PartnerOrganisation not found: " + partnerId));
        if (body.containsKey("name")) p.setName((String) body.get("name"));
        if (body.containsKey("country")) p.setCountry((String) body.get("country"));
        if (body.containsKey("roleInProject")) p.setRoleInProject((String) body.get("roleInProject"));
        if (body.containsKey("contactPerson")) p.setContactPerson((String) body.get("contactPerson"));
        if (body.containsKey("contactEmail")) p.setContactEmail((String) body.get("contactEmail"));
        if (body.containsKey("budgetShare") && body.get("budgetShare") != null) p.setBudgetShare(((Number) body.get("budgetShare")).doubleValue());
        if (body.containsKey("notes")) p.setNotes((String) body.get("notes"));
        return ResponseEntity.ok(partnerToMap(partnerRepo.save(p)));
    }

    @DeleteMapping("/partners/{partnerId}")
    public ResponseEntity<Void> deletePartner(@PathVariable String projectId, @PathVariable String partnerId) {
        partnerRepo.deleteById(partnerId);
        return ResponseEntity.noContent().build();
    }

    // =========== MAPPERS ===========

    private Map<String, Object> stakeholderToMap(Stakeholder s) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id", s.getId());
        m.put("projectId", s.getProjectId());
        m.put("name", s.getName());
        m.put("organization", s.getOrganization());
        m.put("roleDescription", s.getRoleDescription());
        m.put("influenceLevel", s.getInfluenceLevel());
        m.put("interestLevel", s.getInterestLevel());
        m.put("engagementStrategy", s.getEngagementStrategy());
        m.put("channel", s.getChannel());
        m.put("notes", s.getNotes());
        m.put("createdAt", s.getCreatedAt());
        return m;
    }

    private Map<String, Object> sponsorToMap(Sponsor s) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id", s.getId());
        m.put("name", s.getName());
        m.put("organization", s.getOrganization());
        m.put("email", s.getEmail());
        m.put("contactPerson", s.getContactPerson());
        m.put("type", s.getType());
        m.put("notes", s.getNotes());
        m.put("createdAt", s.getCreatedAt());
        return m;
    }

    private Map<String, Object> commitToMap(SponsorCommitment c) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id", c.getId());
        m.put("sponsorId", c.getSponsorId());
        m.put("projectId", c.getProjectId());
        m.put("description", c.getDescription());
        m.put("committedAmount", c.getCommittedAmount());
        m.put("currency", c.getCurrency());
        m.put("status", c.getStatus());
        m.put("agreementDate", c.getAgreementDate());
        m.put("agreementAssetId", c.getAgreementAssetId());
        return m;
    }

    private Map<String, Object> donorToMap(Donor d) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id", d.getId());
        m.put("name", d.getName());
        m.put("organization", d.getOrganization());
        m.put("email", d.getEmail());
        m.put("phone", d.getPhone());
        m.put("type", d.getType());
        m.put("notes", d.getNotes());
        m.put("createdAt", d.getCreatedAt());
        return m;
    }

    private Map<String, Object> donationToMap(Donation d) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id", d.getId());
        m.put("donorId", d.getDonorId());
        m.put("projectId", d.getProjectId());
        m.put("amount", d.getAmount());
        m.put("currency", d.getCurrency());
        m.put("donationDate", d.getDonationDate());
        m.put("isRestricted", d.getIsRestricted());
        m.put("restrictionDescription", d.getRestrictionDescription());
        m.put("receiptAssetId", d.getReceiptAssetId());
        m.put("notes", d.getNotes());
        m.put("createdAt", d.getCreatedAt());
        return m;
    }

    private Map<String, Object> partnerToMap(PartnerOrganisation p) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id", p.getId());
        m.put("projectId", p.getProjectId());
        m.put("name", p.getName());
        m.put("country", p.getCountry());
        m.put("roleInProject", p.getRoleInProject());
        m.put("contactPerson", p.getContactPerson());
        m.put("contactEmail", p.getContactEmail());
        m.put("budgetShare", p.getBudgetShare());
        m.put("notes", p.getNotes());
        m.put("createdAt", p.getCreatedAt());
        return m;
    }
}
