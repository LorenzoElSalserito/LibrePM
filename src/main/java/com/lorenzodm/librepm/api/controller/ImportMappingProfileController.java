package com.lorenzodm.librepm.api.controller;

import com.lorenzodm.librepm.core.entity.ImportMappingProfile;
import com.lorenzodm.librepm.service.ImportMappingProfileService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/import-mapping-profiles")
public class ImportMappingProfileController {

    private final ImportMappingProfileService mappingService;

    public ImportMappingProfileController(ImportMappingProfileService mappingService) {
        this.mappingService = mappingService;
    }

    @GetMapping
    public ResponseEntity<List<Map<String, Object>>> list(
            @RequestParam(required = false) String entityType) {
        List<ImportMappingProfile> profiles = entityType != null
                ? mappingService.listByEntityType(entityType)
                : mappingService.listAll();
        return ResponseEntity.ok(profiles.stream().map(this::toMap).toList());
    }

    @PostMapping
    public ResponseEntity<Map<String, Object>> create(@RequestBody Map<String, Object> body) {
        ImportMappingProfile p = new ImportMappingProfile();
        p.setName((String) body.get("name"));
        p.setEntityType((String) body.get("entityType"));
        p.setMappingJson((String) body.get("mappingJson"));
        if (body.get("createdBy") != null) p.setCreatedBy((String) body.get("createdBy"));
        return ResponseEntity.ok(toMap(mappingService.create(p)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Map<String, Object>> update(
            @PathVariable String id, @RequestBody Map<String, Object> body) {
        ImportMappingProfile updated = new ImportMappingProfile();
        if (body.get("name") != null) updated.setName((String) body.get("name"));
        if (body.get("entityType") != null) updated.setEntityType((String) body.get("entityType"));
        if (body.get("mappingJson") != null) updated.setMappingJson((String) body.get("mappingJson"));
        return ResponseEntity.ok(toMap(mappingService.update(id, updated)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable String id) {
        mappingService.delete(id);
        return ResponseEntity.noContent().build();
    }

    private Map<String, Object> toMap(ImportMappingProfile p) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id", p.getId());
        m.put("name", p.getName());
        m.put("entityType", p.getEntityType());
        m.put("mappingJson", p.getMappingJson());
        m.put("createdBy", p.getCreatedBy());
        m.put("createdAt", p.getCreatedAt() != null ? p.getCreatedAt().toString() : null);
        return m;
    }
}
