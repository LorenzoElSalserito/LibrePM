package com.lorenzodm.librepm.api.controller;

import com.lorenzodm.librepm.api.dto.request.UpdateUserSettingsRequest;
import com.lorenzodm.librepm.api.dto.response.UserSettingsResponse;
import com.lorenzodm.librepm.api.mapper.UserSettingsMapper;
import com.lorenzodm.librepm.core.entity.UserSettings;
import com.lorenzodm.librepm.security.CurrentUser;
import com.lorenzodm.librepm.service.UserSettingsService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Controller REST per gestione UserSettings
 * 
 * Endpoints:
 * - GET  /api/settings        - Ottieni settings utente
 * - PUT  /api/settings        - Aggiorna settings
 * - POST /api/settings/reset  - Reset ai valori default
 *
 * @author Lorenzo DM
 * @since 0.2.0
 */
@RestController
@RequestMapping("/api/settings")
public class UserSettingsController {

    private final UserSettingsService settingsService;
    private final UserSettingsMapper settingsMapper;

    public UserSettingsController(
            UserSettingsService settingsService,
            UserSettingsMapper settingsMapper) {
        this.settingsService = settingsService;
        this.settingsMapper = settingsMapper;
    }

    /**
     * Ottieni settings utente (crea se non esistono)
     */
    @GetMapping
    public ResponseEntity<UserSettingsResponse> getSettings(@CurrentUser String userId) {
        UserSettings settings = settingsService.getOrCreateSettings(userId);
        return ResponseEntity.ok(settingsMapper.toResponse(settings));
    }

    /**
     * Aggiorna settings utente
     */
    @PutMapping
    public ResponseEntity<UserSettingsResponse> updateSettings(
            @CurrentUser String userId,
            @Valid @RequestBody UpdateUserSettingsRequest request) {
        
        UserSettings settings = settingsService.updateSettings(userId, request);
        return ResponseEntity.ok(settingsMapper.toResponse(settings));
    }

    /**
     * Reset settings ai valori default
     */
    @PostMapping("/reset")
    public ResponseEntity<UserSettingsResponse> resetSettings(@CurrentUser String userId) {
        UserSettings settings = settingsService.resetToDefaults(userId);
        return ResponseEntity.ok(settingsMapper.toResponse(settings));
    }
}
