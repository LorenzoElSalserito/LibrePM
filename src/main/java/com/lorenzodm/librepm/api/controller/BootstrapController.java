package com.lorenzodm.librepm.api.controller;

import com.lorenzodm.librepm.api.dto.request.CreateLocalProfileRequest;
import com.lorenzodm.librepm.api.dto.request.LoginRequest;
import com.lorenzodm.librepm.api.dto.request.UpdateAppPreferencesRequest;
import com.lorenzodm.librepm.api.dto.response.BootstrapResponse;
import com.lorenzodm.librepm.api.dto.response.UserResponse;
import com.lorenzodm.librepm.api.mapper.UserMapper;
import com.lorenzodm.librepm.core.entity.User;
import com.lorenzodm.librepm.service.BootstrapService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;

/**
 * REST Controller for Application Bootstrap.
 * <p>
 * This controller handles the initial handshake between the frontend and backend.
 * It provides endpoints for:
 * <ul>
 *     <li>Retrieving initial system state and user profiles</li>
 *     <li>Creating and managing local user profiles</li>
 *     <li>Managing global application preferences</li>
 * </ul>
 * These endpoints are publicly accessible (no auth required) as they are needed
 * before a user session is established.
 * </p>
 *
 * @author Lorenzo DM
 * @since 0.3.0
 * @version 0.5.3
 */
@RestController
@RequestMapping("/api/bootstrap")
public class BootstrapController {

    private static final Logger log = LoggerFactory.getLogger(BootstrapController.class);

    private final BootstrapService bootstrapService;
    private final UserMapper userMapper;

    public BootstrapController(BootstrapService bootstrapService, UserMapper userMapper) {
        this.bootstrapService = bootstrapService;
        this.userMapper = userMapper;
    }

    /**
     * GET /api/bootstrap
     * <p>
     * Retrieves all necessary data to initialize the application frontend.
     * This includes the list of available profiles, system info, and preferences.
     * </p>
     *
     * @return 200 OK with {@link BootstrapResponse}.
     */
    @GetMapping
    public ResponseEntity<BootstrapResponse> getBootstrap() {
        log.debug("Richiesta bootstrap");
        BootstrapResponse response = bootstrapService.getBootstrapData();
        return ResponseEntity.ok(response);
    }

    /**
     * POST /api/bootstrap/profiles
     * <p>
     * Creates a new local user profile. Local profiles do not require a password
     * for authentication in the desktop version.
     * </p>
     *
     * @param request The profile creation details.
     * @return 201 Created with the new {@link UserResponse}.
     */
    @PostMapping("/profiles")
    public ResponseEntity<UserResponse> createProfile(
            @Valid @RequestBody CreateLocalProfileRequest request) {

        log.info("Creazione nuovo profilo: {}", request.username());
        User created = bootstrapService.createLocalProfile(request);

        return ResponseEntity
                .created(URI.create("/api/users/" + created.getId()))
                .body(userMapper.toResponse(created));
    }

    /**
     * POST /api/bootstrap/select/{userId}
     * <p>
     * Selects a specific profile as the active one for the current session.
     * Updates the last login timestamp.
     * </p>
     *
     * @param userId The ID of the user to select.
     * @return 200 OK with the selected {@link UserResponse}.
     */
    @PostMapping("/select/{userId}")
    public ResponseEntity<UserResponse> selectProfile(@PathVariable String userId) {
        log.info("Selezione profilo: {}", userId);
        User selected = bootstrapService.selectProfile(userId);
        return ResponseEntity.ok(userMapper.toResponseLight(selected));
    }

    /**
     * POST /api/bootstrap/login
     * <p>
     * Authenticates a user with password and selects the profile.
     * </p>
     *
     * @param request The login request containing userId and password.
     * @return 200 OK with the authenticated {@link UserResponse}.
     */
    @PostMapping("/login")
    public ResponseEntity<UserResponse> login(@Valid @RequestBody LoginRequest request) {
        log.info("Richiesta login per utente: {}", request.userId());
        User user = bootstrapService.login(request.userId(), request.password());
        return ResponseEntity.ok(userMapper.toResponseLight(user));
    }

    /**
     * DELETE /api/bootstrap/profiles/{userId}
     * <p>
     * Permanently deletes a local profile and all associated data.
     * </p>
     *
     * @param userId The ID of the user to delete.
     * @return 204 No Content.
     */
    @DeleteMapping("/profiles/{userId}")
    public ResponseEntity<Void> deleteProfile(@PathVariable String userId) {
        log.info("Eliminazione profilo: {}", userId);
        bootstrapService.deleteProfile(userId);
        return ResponseEntity.noContent().build();
    }

    /**
     * GET /api/bootstrap/preferences
     * <p>
     * Retrieves the global application preferences (e.g., autologin settings).
     * </p>
     *
     * @return 200 OK with {@link BootstrapResponse.AppPreferencesResponse}.
     */
    @GetMapping("/preferences")
    public ResponseEntity<BootstrapResponse.AppPreferencesResponse> getPreferences() {
        return ResponseEntity.ok(bootstrapService.getPreferences());
    }

    /**
     * PUT /api/bootstrap/preferences
     * <p>
     * Updates the global application preferences.
     * </p>
     *
     * @param request The preferences to update.
     * @return 200 OK with the updated preferences.
     */
    @PutMapping("/preferences")
    public ResponseEntity<BootstrapResponse.AppPreferencesResponse> updatePreferences(
            @Valid @RequestBody UpdateAppPreferencesRequest request) {

        log.debug("Aggiornamento preferenze: {}", request);
        BootstrapResponse.AppPreferencesResponse updated =
                bootstrapService.updatePreferences(request);
        return ResponseEntity.ok(updated);
    }

    /**
     * GET /api/bootstrap/validate/{userId}
     * <p>
     * Checks if a user ID is valid and active, typically used for autologin validation.
     * </p>
     *
     * @param userId The ID to validate.
     * @return 200 OK with validation result.
     */
    @GetMapping("/validate/{userId}")
    public ResponseEntity<ValidationResponse> validateForAutologin(@PathVariable String userId) {
        boolean valid = bootstrapService.isValidForAutologin(userId);
        return ResponseEntity.ok(new ValidationResponse(userId, valid));
    }

    /**
     * POST /api/bootstrap/autodetect
     * <p>
     * Triggers an auto-detection of existing data (e.g., after connecting to an external DB).
     * Useful for recovering state if preferences are lost but data exists.
     * </p>
     *
     * @return 200 OK.
     */
    @PostMapping("/autodetect")
    public ResponseEntity<Void> autoDetectData() {
        log.info("Richiesto auto-detect dati");
        bootstrapService.autoDetectData();
        return ResponseEntity.ok().build();
    }

    /**
     * Internal DTO for validation response.
     */
    private record ValidationResponse(String userId, boolean valid) {}
}
