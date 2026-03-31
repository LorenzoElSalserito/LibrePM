package com.lorenzodm.librepm.api.dto.response;

public record NotificationPreferenceResponse(
        String id,
        String eventType,
        String channel,
        boolean enabled,
        String severityThreshold
) {}
