package com.lorenzodm.librepm.api.controller;

import com.lorenzodm.librepm.api.dto.response.NotificationResponse;
import com.lorenzodm.librepm.api.mapper.NotificationMapper;
import com.lorenzodm.librepm.core.entity.Notification;
import com.lorenzodm.librepm.service.NotificationService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/notifications")
public class NotificationController {

    private final NotificationService notificationService;
    private final NotificationMapper notificationMapper;

    public NotificationController(NotificationService notificationService, NotificationMapper notificationMapper) {
        this.notificationService = notificationService;
        this.notificationMapper = notificationMapper;
    }

    @GetMapping("/unread")
    public ResponseEntity<List<NotificationResponse>> listUnread(@RequestHeader("X-User-Id") String userId) {
        List<Notification> notifications = notificationService.listUnread(userId);
        return ResponseEntity.ok(notifications.stream()
                .map(notificationMapper::toResponse)
                .collect(Collectors.toList()));
    }
    
    @GetMapping("/count")
    public ResponseEntity<Long> countUnread(@RequestHeader("X-User-Id") String userId) {
        return ResponseEntity.ok(notificationService.countUnread(userId));
    }

    @PostMapping("/{id}/read")
    public ResponseEntity<Void> markAsRead(@RequestHeader("X-User-Id") String userId, @PathVariable String id) {
        notificationService.markAsRead(userId, id);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/read-all")
    public ResponseEntity<Void> markAllAsRead(@RequestHeader("X-User-Id") String userId) {
        notificationService.markAllAsRead(userId);
        return ResponseEntity.ok().build();
    }
}
