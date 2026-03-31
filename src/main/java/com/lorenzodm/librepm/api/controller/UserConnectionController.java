package com.lorenzodm.librepm.api.controller;

import com.lorenzodm.librepm.api.dto.response.UserConnectionResponse;
import com.lorenzodm.librepm.api.dto.response.UserResponse;
import com.lorenzodm.librepm.api.mapper.UserMapper;
import com.lorenzodm.librepm.service.UserConnectionService;
import com.lorenzodm.librepm.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/connections")
public class UserConnectionController {

    private static final Logger log = LoggerFactory.getLogger(UserConnectionController.class);

    private final UserConnectionService connectionService;
    private final UserMapper userMapper;
    // Inject UserService to get current user if needed, but we pass userId from frontend for now

    public UserConnectionController(UserConnectionService connectionService, UserMapper userMapper) {
        this.connectionService = connectionService;
        this.userMapper = userMapper;
    }

    @GetMapping
    public ResponseEntity<List<UserResponse>> listFriends(@RequestHeader("X-User-Id") String userId) {
        log.debug("Listing friends for user {}", userId);
        List<UserResponse> friends = connectionService.listFriends(userId).stream()
                .map(userMapper::toResponseLight)
                .collect(Collectors.toList());
        return ResponseEntity.ok(friends);
    }

    @GetMapping("/search")
    public ResponseEntity<List<UserResponse>> searchFriends(@RequestHeader("X-User-Id") String userId, @RequestParam String q) {
        log.debug("Searching friends for user {} with query {}", userId, q);
        List<UserResponse> friends = connectionService.searchFriends(userId, q).stream()
                .map(userMapper::toResponseLight)
                .collect(Collectors.toList());
        return ResponseEntity.ok(friends);
    }

    @PostMapping("/request/{targetId}")
    public ResponseEntity<Void> sendRequest(@RequestHeader("X-User-Id") String userId, @PathVariable String targetId) {
        log.debug("User {} sending request to {}", userId, targetId);
        connectionService.sendRequest(userId, targetId);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{connectionId}/accept")
    public ResponseEntity<Void> acceptRequest(@RequestHeader("X-User-Id") String userId, @PathVariable String connectionId) {
        log.debug("User {} accepting connection {}", userId, connectionId);
        connectionService.acceptRequest(userId, connectionId);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{connectionId}/reject")
    public ResponseEntity<Void> rejectRequest(@RequestHeader("X-User-Id") String userId, @PathVariable String connectionId) {
        log.debug("User {} rejecting connection {}", userId, connectionId);
        connectionService.rejectRequest(userId, connectionId);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/remove/{targetId}")
    public ResponseEntity<Void> removeConnection(@RequestHeader("X-User-Id") String userId, @PathVariable String targetId) {
        log.debug("User {} removing connection with {}", userId, targetId);
        connectionService.removeConnection(userId, targetId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/pending/incoming")
    public ResponseEntity<List<UserConnectionResponse>> listPendingIncoming(@RequestHeader("X-User-Id") String userId) {
        log.debug("Listing pending incoming for user {}", userId);
        List<UserConnectionResponse> pending = connectionService.listPendingIncoming(userId).stream()
                .map(c -> new UserConnectionResponse(
                        c.getId(),
                        userMapper.toResponseLight(c.getRequester()),
                        c.getStatus().name(),
                        c.getCreatedAt()
                ))
                .collect(Collectors.toList());
        return ResponseEntity.ok(pending);
    }
    
    @GetMapping("/ghosts")
    public ResponseEntity<List<UserResponse>> listGhosts(@RequestHeader("X-User-Id") String userId) {
        log.debug("Listing ghost users for user {}", userId);
        List<UserResponse> ghosts = connectionService.listGhosts(userId).stream()
                .map(userMapper::toResponseLight)
                .collect(Collectors.toList());
        return ResponseEntity.ok(ghosts);
    }

    @PostMapping("/ghosts")
    public ResponseEntity<UserResponse> createGhost(
            @RequestHeader("X-User-Id") String userId,
            @RequestBody Map<String, String> body) {
        String username = body.get("username");
        String displayName = body.get("displayName");
        log.debug("Creating global ghost user '{}' for user {}", username, userId);
        var ghost = connectionService.createGhostGlobal(userId, username, displayName);
        return ResponseEntity.ok(userMapper.toResponseLight(ghost));
    }

    @PutMapping("/ghosts/{ghostId}")
    public ResponseEntity<UserResponse> updateGhost(
            @RequestHeader("X-User-Id") String userId,
            @PathVariable String ghostId,
            @RequestBody Map<String, String> body) {
        String username = body.get("username");
        String displayName = body.get("displayName");
        log.debug("User {} updating ghost {}", userId, ghostId);
        var ghost = connectionService.updateGhost(userId, ghostId, username, displayName);
        return ResponseEntity.ok(userMapper.toResponseLight(ghost));
    }

    @DeleteMapping("/ghosts/{ghostId}")
    public ResponseEntity<Void> deleteGhost(
            @RequestHeader("X-User-Id") String userId,
            @PathVariable String ghostId) {
        log.debug("User {} deleting ghost {}", userId, ghostId);
        connectionService.deleteGhost(userId, ghostId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/pending/outgoing")
    public ResponseEntity<List<UserConnectionResponse>> listPendingOutgoing(@RequestHeader("X-User-Id") String userId) {
        log.debug("Listing pending outgoing for user {}", userId);
        List<UserConnectionResponse> pending = connectionService.listPendingOutgoing(userId).stream()
                .map(c -> new UserConnectionResponse(
                        c.getId(),
                        userMapper.toResponseLight(c.getTarget()),
                        c.getStatus().name(),
                        c.getCreatedAt()
                ))
                .collect(Collectors.toList());
        return ResponseEntity.ok(pending);
    }
}
