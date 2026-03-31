package com.lorenzodm.librepm.api.dto.response;

import java.time.Instant;

/**
 * Response DTO per UserSettings
 *
 * @author Lorenzo DM
 * @since 0.2.0
 */
public record UserSettingsResponse(
        String userId,
        String theme,
        String language,
        boolean notificationsEnabled,
        int focusTimerDefaultMinutes,
        boolean autoBackupEnabled,
        Instant lastBackupAt,
        Instant createdAt,
        Instant updatedAt
) {
}
