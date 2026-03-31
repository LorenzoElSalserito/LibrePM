package com.lorenzodm.librepm.service;

import com.lorenzodm.librepm.api.dto.request.UpdateUserSettingsRequest;
import com.lorenzodm.librepm.core.entity.UserSettings;

/**
 * Service per gestione UserSettings
 *
 * @author Lorenzo DM
 * @since 0.2.0
 */
public interface UserSettingsService {

    /**
     * Ottieni settings utente (crea se non esistono)
     */
    UserSettings getOrCreateSettings(String userId);

    /**
     * Aggiorna settings utente
     */
    UserSettings updateSettings(String userId, UpdateUserSettingsRequest request);

    /**
     * Reset settings ai valori default
     */
    UserSettings resetToDefaults(String userId);

    /**
     * Aggiorna timestamp ultimo backup
     */
    void updateLastBackup(String userId);
}
