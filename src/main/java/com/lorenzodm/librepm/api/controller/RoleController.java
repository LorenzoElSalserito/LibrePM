package com.lorenzodm.librepm.api.controller;

import com.lorenzodm.librepm.api.dto.request.CreateRoleRequest;
import com.lorenzodm.librepm.api.dto.response.RoleResponse;
import com.lorenzodm.librepm.api.mapper.RoleMapper;
import com.lorenzodm.librepm.core.entity.Role;
import com.lorenzodm.librepm.service.RoleService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/roles")
public class RoleController {

    private final RoleService service;
    private final RoleMapper mapper;

    public RoleController(RoleService service, RoleMapper mapper) {
        this.service = service;
        this.mapper = mapper;
    }

    @PostMapping
    public ResponseEntity<RoleResponse> create(@Valid @RequestBody CreateRoleRequest request) {
        Role role = service.create(request);
        List<String> permissions = service.getPermissions(role.getId());
        return ResponseEntity.status(HttpStatus.CREATED).body(mapper.toResponse(role, permissions));
    }

    @GetMapping
    public ResponseEntity<List<RoleResponse>> listAll() {
        List<RoleResponse> response = service.listAll().stream()
                .map(r -> mapper.toResponse(r, service.getPermissions(r.getId())))
                .toList();
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<RoleResponse> getById(@PathVariable String id) {
        Role role = service.getById(id);
        List<String> permissions = service.getPermissions(id);
        return ResponseEntity.ok(mapper.toResponse(role, permissions));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable String id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/permissions")
    public ResponseEntity<Void> addPermission(@PathVariable String id, @RequestBody String permission) {
        service.addPermission(id, permission.trim().replace("\"", ""));
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @DeleteMapping("/{id}/permissions/{permission}")
    public ResponseEntity<Void> removePermission(@PathVariable String id, @PathVariable String permission) {
        service.removePermission(id, permission);
        return ResponseEntity.noContent().build();
    }
}
