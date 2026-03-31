package com.lorenzodm.librepm.api.controller;

import com.lorenzodm.librepm.core.entity.CapacityProfile;
import com.lorenzodm.librepm.core.entity.LeaveRecord;
import com.lorenzodm.librepm.core.entity.User;
import com.lorenzodm.librepm.core.entity.WorkCalendar;
import com.lorenzodm.librepm.repository.UserRepository;
import com.lorenzodm.librepm.repository.WorkCalendarRepository;
import com.lorenzodm.librepm.service.CapacityService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/users/{userId}/capacity")
public class CapacityController {

    private final CapacityService capacityService;
    private final UserRepository userRepo;
    private final WorkCalendarRepository calendarRepo;

    public CapacityController(CapacityService capacityService, UserRepository userRepo,
                              WorkCalendarRepository calendarRepo) {
        this.capacityService = capacityService;
        this.userRepo = userRepo;
        this.calendarRepo = calendarRepo;
    }

    // --- Capacity Profiles ---

    @GetMapping("/profiles")
    public ResponseEntity<List<Map<String, Object>>> listProfiles(@PathVariable String userId) {
        return ResponseEntity.ok(
                capacityService.listByUser(userId).stream().map(this::profileToMap).toList()
        );
    }

    @PostMapping("/profiles")
    public ResponseEntity<Map<String, Object>> createProfile(
            @PathVariable String userId, @RequestBody Map<String, Object> body) {
        User user = userRepo.findById(userId).orElseThrow();
        CapacityProfile cp = new CapacityProfile();
        cp.setUser(user);
        cp.setHoursPerDay(body.containsKey("hoursPerDay") ? ((Number) body.get("hoursPerDay")).doubleValue() : 8.0);
        cp.setEffectiveFrom(LocalDate.parse((String) body.get("effectiveFrom")));
        if (body.get("effectiveTo") != null) cp.setEffectiveTo(LocalDate.parse((String) body.get("effectiveTo")));
        if (body.get("calendarId") != null) {
            calendarRepo.findById((String) body.get("calendarId")).ifPresent(cp::setCalendar);
        }
        return ResponseEntity.ok(profileToMap(capacityService.create(cp)));
    }

    @DeleteMapping("/profiles/{profileId}")
    public ResponseEntity<Void> deleteProfile(@PathVariable String profileId) {
        capacityService.delete(profileId);
        return ResponseEntity.noContent().build();
    }

    // --- Leave Records ---

    @GetMapping("/leave")
    public ResponseEntity<List<Map<String, Object>>> listLeave(@PathVariable String userId,
            @RequestParam(required = false) String from, @RequestParam(required = false) String to) {
        List<LeaveRecord> records;
        if (from != null && to != null) {
            records = capacityService.listLeaveByUserBetween(userId, LocalDate.parse(from), LocalDate.parse(to));
        } else {
            records = capacityService.listLeaveByUser(userId);
        }
        return ResponseEntity.ok(records.stream().map(this::leaveToMap).toList());
    }

    @PostMapping("/leave")
    public ResponseEntity<Map<String, Object>> createLeave(
            @PathVariable String userId, @RequestBody Map<String, Object> body) {
        User user = userRepo.findById(userId).orElseThrow();
        LeaveRecord lr = new LeaveRecord();
        lr.setUser(user);
        lr.setLeaveDate(LocalDate.parse((String) body.get("leaveDate")));
        if (body.get("leaveType") != null) lr.setLeaveType(LeaveRecord.LeaveType.valueOf((String) body.get("leaveType")));
        if (body.get("hours") != null) lr.setHours(((Number) body.get("hours")).doubleValue());
        if (body.get("description") != null) lr.setDescription((String) body.get("description"));
        return ResponseEntity.ok(leaveToMap(capacityService.createLeave(lr)));
    }

    @DeleteMapping("/leave/{leaveId}")
    public ResponseEntity<Void> deleteLeave(@PathVariable String leaveId) {
        capacityService.deleteLeave(leaveId);
        return ResponseEntity.noContent().build();
    }

    // --- Computed Capacity ---

    @GetMapping("/available")
    public ResponseEntity<Map<String, Object>> getAvailable(
            @PathVariable String userId,
            @RequestParam String from, @RequestParam String to) {
        LocalDate fromDate = LocalDate.parse(from);
        LocalDate toDate = LocalDate.parse(to);
        double total = capacityService.getAvailableHoursInRange(userId, fromDate, toDate);
        return ResponseEntity.ok(Map.of("userId", userId, "from", from, "to", to, "availableHours", total));
    }

    private Map<String, Object> profileToMap(CapacityProfile cp) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id", cp.getId());
        m.put("userId", cp.getUser().getId());
        m.put("hoursPerDay", cp.getHoursPerDay());
        m.put("effectiveFrom", cp.getEffectiveFrom().toString());
        m.put("effectiveTo", cp.getEffectiveTo() != null ? cp.getEffectiveTo().toString() : null);
        m.put("calendarId", cp.getCalendar() != null ? cp.getCalendar().getId() : null);
        return m;
    }

    private Map<String, Object> leaveToMap(LeaveRecord lr) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id", lr.getId());
        m.put("userId", lr.getUser().getId());
        m.put("leaveDate", lr.getLeaveDate().toString());
        m.put("leaveType", lr.getLeaveType() != null ? lr.getLeaveType().name() : null);
        m.put("hours", lr.getHours());
        m.put("description", lr.getDescription());
        return m;
    }
}
