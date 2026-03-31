package com.lorenzodm.librepm.api.mapper;

import com.lorenzodm.librepm.api.dto.response.UserSettingsResponse;
import com.lorenzodm.librepm.core.entity.UserSettings;
import org.springframework.stereotype.Component;

/**
 * Mapper per UserSettings entity
 *
 * @author Lorenzo DM
 * @since 0.2.0
 */
@Component
public class UserSettingsMapper {

    public UserSettingsResponse toResponse(UserSettings settings) {
        if (settings == null) return null;

        return new UserSettingsResponse(
                settings.getUserId(),
                settings.getTheme(),
                settings.getLanguage(),
                settings.isNotificationsEnabled(),
                settings.getFocusTimerDefaultMinutes(),
                settings.isAutoBackupEnabled(),
                settings.getLastBackupAt(),
                settings.getCreatedAt(),
                settings.getUpdatedAt()
        );
    }
}
