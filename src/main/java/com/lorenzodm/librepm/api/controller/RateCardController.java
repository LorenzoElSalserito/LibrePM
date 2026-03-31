package com.lorenzodm.librepm.api.controller;

import com.lorenzodm.librepm.core.entity.RateCard;
import com.lorenzodm.librepm.service.RateCardService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/rate-cards")
public class RateCardController {

    private final RateCardService rateCardService;

    public RateCardController(RateCardService rateCardService) {
        this.rateCardService = rateCardService;
    }

    @GetMapping
    public ResponseEntity<List<Map<String, Object>>> listByScope(
            @RequestParam String scope, @RequestParam String entityId) {
        return ResponseEntity.ok(
                rateCardService.listByScope(RateCard.Scope.valueOf(scope), entityId)
                        .stream().map(this::toMap).toList()
        );
    }

    @PostMapping
    public ResponseEntity<Map<String, Object>> create(@RequestBody Map<String, Object> body) {
        RateCard rc = new RateCard();
        rc.setScope(RateCard.Scope.valueOf((String) body.get("scope")));
        rc.setScopeEntityId((String) body.get("scopeEntityId"));
        if (body.get("currency") != null) rc.setCurrency((String) body.get("currency"));
        rc.setHourlyRate(((Number) body.get("hourlyRate")).doubleValue());
        if (body.get("dailyRate") != null) rc.setDailyRate(((Number) body.get("dailyRate")).doubleValue());
        rc.setEffectiveFrom(LocalDate.parse((String) body.get("effectiveFrom")));
        if (body.get("effectiveTo") != null) rc.setEffectiveTo(LocalDate.parse((String) body.get("effectiveTo")));
        return ResponseEntity.ok(toMap(rateCardService.create(rc)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Map<String, Object>> update(
            @PathVariable String id, @RequestBody Map<String, Object> body) {
        RateCard updated = new RateCard();
        if (body.get("scope") != null) updated.setScope(RateCard.Scope.valueOf((String) body.get("scope")));
        if (body.get("scopeEntityId") != null) updated.setScopeEntityId((String) body.get("scopeEntityId"));
        if (body.get("currency") != null) updated.setCurrency((String) body.get("currency"));
        updated.setHourlyRate(body.containsKey("hourlyRate") ? ((Number) body.get("hourlyRate")).doubleValue() : 0);
        if (body.get("dailyRate") != null) updated.setDailyRate(((Number) body.get("dailyRate")).doubleValue());
        if (body.get("effectiveFrom") != null) updated.setEffectiveFrom(LocalDate.parse((String) body.get("effectiveFrom")));
        if (body.get("effectiveTo") != null) updated.setEffectiveTo(LocalDate.parse((String) body.get("effectiveTo")));
        return ResponseEntity.ok(toMap(rateCardService.update(id, updated)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable String id) {
        rateCardService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/resolve")
    public ResponseEntity<?> resolve(
            @RequestParam String userId,
            @RequestParam(required = false) String projectId,
            @RequestParam(required = false) String roleId,
            @RequestParam String date) {
        return rateCardService.resolveRate(userId, projectId, roleId, LocalDate.parse(date))
                .map(rc -> ResponseEntity.ok(toMap(rc)))
                .orElse(ResponseEntity.noContent().build());
    }

    private Map<String, Object> toMap(RateCard rc) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id", rc.getId());
        m.put("scope", rc.getScope().name());
        m.put("scopeEntityId", rc.getScopeEntityId());
        m.put("currency", rc.getCurrency());
        m.put("hourlyRate", rc.getHourlyRate());
        m.put("dailyRate", rc.getDailyRate());
        m.put("effectiveFrom", rc.getEffectiveFrom().toString());
        m.put("effectiveTo", rc.getEffectiveTo() != null ? rc.getEffectiveTo().toString() : null);
        m.put("createdAt", rc.getCreatedAt() != null ? rc.getCreatedAt().toString() : null);
        return m;
    }
}
