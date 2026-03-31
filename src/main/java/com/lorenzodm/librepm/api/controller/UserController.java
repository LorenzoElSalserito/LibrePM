package com.lorenzodm.librepm.api.controller;

import com.lorenzodm.librepm.api.dto.request.CreateUserRequest;
import com.lorenzodm.librepm.api.dto.request.UpdateUserRequest;
import com.lorenzodm.librepm.api.dto.response.UserResponse;
import com.lorenzodm.librepm.api.mapper.UserMapper;
import com.lorenzodm.librepm.core.entity.User;
import com.lorenzodm.librepm.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;
    private final UserMapper userMapper;

    public UserController(UserService userService, UserMapper userMapper) {
        this.userService = userService;
        this.userMapper = userMapper;
    }

    @PostMapping
    public ResponseEntity<UserResponse> create(@Valid @RequestBody CreateUserRequest req) {
        User created = userService.create(req);
        return ResponseEntity.created(URI.create("/api/users/" + created.getId()))
                .body(userMapper.toResponse(created));
    }

    @GetMapping("/{userId}")
    public ResponseEntity<UserResponse> get(@PathVariable String userId) {
        return ResponseEntity.ok(userMapper.toResponse(userService.getById(userId)));
    }

    @GetMapping
    public ResponseEntity<List<UserResponse>> list(@RequestParam(defaultValue = "true") boolean onlyActive) {
        List<UserResponse> out = userService.list(onlyActive).stream().map(userMapper::toResponse).toList();
        return ResponseEntity.ok(out);
    }

    @GetMapping("/search")
    public ResponseEntity<List<UserResponse>> search(@RequestParam String q) {
        List<UserResponse> out = userService.search(q).stream().map(userMapper::toResponse).toList();
        return ResponseEntity.ok(out);
    }

    @PutMapping("/{userId}")
    public ResponseEntity<UserResponse> update(@PathVariable String userId, @Valid @RequestBody UpdateUserRequest req) {
        return ResponseEntity.ok(userMapper.toResponse(userService.update(userId, req)));
    }

    @PatchMapping("/{userId}/active")
    public ResponseEntity<UserResponse> setActive(@PathVariable String userId, @RequestParam boolean active) {
        return ResponseEntity.ok(userMapper.toResponse(userService.setActive(userId, active)));
    }

    @PutMapping("/{userId}/change-password")
    public ResponseEntity<Void> changePassword(
            @PathVariable String userId,
            @RequestBody Map<String, String> body) {
        userService.changePassword(userId, body.get("oldPassword"), body.get("newPassword"));
        return ResponseEntity.ok().build();
    }

    @PutMapping("/{userId}/remove-password")
    public ResponseEntity<Void> removePassword(
            @PathVariable String userId,
            @RequestBody Map<String, String> body) {
        userService.removePassword(userId, body.get("currentPassword"));
        return ResponseEntity.ok().build();
    }
}
