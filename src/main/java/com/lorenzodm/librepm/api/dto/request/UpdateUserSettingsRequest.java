package com.lorenzodm.librepm.api.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Pattern;

/**
 * Request per aggiornamento UserSettings
 *
 * @author Lorenzo DM
 * @since 0.2.0
 */
public record UpdateUserSettingsRequest(
        @Pattern(regexp = "^(dark|light)$", message = "Tema deve essere 'dark' o 'light'")
        String theme,

        @Pattern(regexp = "^(it|en)$", message = "Lingua deve essere 'it' o 'en'")
        String language,

        Boolean notificationsEnabled,

        @Min(value = 1, message = "Timer focus deve essere almeno 1 minuto")
        @Max(value = 240, message = "Timer focus non può superare 240 minuti")
        Integer focusTimerDefaultMinutes,

        Boolean autoBackupEnabled,

        @Min(value = 60, message = "Capacità giornaliera minima 1 ora")
        @Max(value = 1440, message = "Capacità giornaliera massima 24 ore")
        Integer dailyWorkCapacityMinutes
) {
}
