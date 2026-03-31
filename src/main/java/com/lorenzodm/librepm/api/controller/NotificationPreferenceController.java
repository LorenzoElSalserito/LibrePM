package com.lorenzodm.librepm.api.controller;

import com.lorenzodm.librepm.api.dto.response.NotificationPreferenceResponse;
import com.lorenzodm.librepm.core.entity.NotificationPreference;
import com.lorenzodm.librepm.repository.NotificationPreferenceRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/users/{userId}/notification-preferences")
public class NotificationPreferenceController {

    private final NotificationPreferenceRepository repository;

    public NotificationPreferenceController(NotificationPreferenceRepository repository) {
        this.repository = repository;
    }

    @GetMapping
    public ResponseEntity<List<NotificationPreferenceResponse>> list(@PathVariable String userId) {
        List<NotificationPreferenceResponse> prefs = repository.findByUserId(userId).stream()
                .map(this::toResponse)
                .toList();
        return ResponseEntity.ok(prefs);
    }

    @PutMapping
    public ResponseEntity<NotificationPreferenceResponse> upsert(
            @PathVariable String userId,
            @RequestBody Map<String, Object> body) {
        String eventType = (String) body.get("eventType");
        String channelStr = (String) body.getOrDefault("channel", "IN_APP");
        Boolean enabled = (Boolean) body.getOrDefault("enabled", true);
        String severity = (String) body.getOrDefault("severityThreshold", "INFO");

        NotificationPreference.Channel channel = NotificationPreference.Channel.valueOf(channelStr);

        NotificationPreference pref = repository
                .findByUserIdAndEventTypeAndChannel(userId, eventType, channel)
                .orElseGet(() -> {
                    NotificationPreference np = new NotificationPreference();
                    np.setUserId(userId);
                    np.setEventType(eventType);
                    np.setChannel(channel);
                    return np;
                });

        pref.setEnabled(enabled);
        if (severity != null) {
            pref.setSeverityThreshold(NotificationPreference.Severity.valueOf(severity));
        }

        repository.save(pref);
        return ResponseEntity.ok(toResponse(pref));
    }

    private NotificationPreferenceResponse toResponse(NotificationPreference p) {
        return new NotificationPreferenceResponse(
                p.getId(),
                p.getEventType(),
                p.getChannel().name(),
                p.isEnabled(),
                p.getSeverityThreshold().name()
        );
    }
}
