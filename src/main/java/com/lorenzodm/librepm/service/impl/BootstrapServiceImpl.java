package com.lorenzodm.librepm.service.impl;

import com.lorenzodm.librepm.api.dto.request.CreateLocalProfileRequest;
import com.lorenzodm.librepm.api.dto.request.UpdateAppPreferencesRequest;
import com.lorenzodm.librepm.api.dto.response.BootstrapResponse;
import com.lorenzodm.librepm.api.dto.response.UserResponse;
import com.lorenzodm.librepm.api.exception.ConflictException;
import com.lorenzodm.librepm.api.exception.ResourceNotFoundException;
import com.lorenzodm.librepm.api.exception.UnauthorizedException;
import com.lorenzodm.librepm.api.mapper.UserMapper;
import com.lorenzodm.librepm.core.entity.AppPreference;
import com.lorenzodm.librepm.core.entity.Asset;
import com.lorenzodm.librepm.core.entity.SyncStatus;
import com.lorenzodm.librepm.core.entity.User;
import com.lorenzodm.librepm.repository.AppPreferenceRepository;
import com.lorenzodm.librepm.repository.AssetRepository;
import com.lorenzodm.librepm.repository.UserRepository;
import com.lorenzodm.librepm.core.entity.AuditEvent;
import com.lorenzodm.librepm.service.AuditService;
import com.lorenzodm.librepm.service.BootstrapService;
import com.lorenzodm.librepm.service.PasswordService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.info.BuildProperties;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * Implementation of the {@link BootstrapService}.
 * <p>
 * Handles the initial data loading and configuration required when the application starts.
 * This includes managing local user profiles, application-wide preferences, and
 * providing system information to the frontend.
 * </p>
 *
 * @author Lorenzo DM
 * @since 0.3.0
 * @version 0.5.3
 */
@Service
@Transactional
public class BootstrapServiceImpl implements BootstrapService {

    private static final Logger log = LoggerFactory.getLogger(BootstrapServiceImpl.class);

    /* Default password for local profiles (not used for auth in desktop mode) */
    private static final String DEFAULT_LOCAL_PASSWORD = "local-profile-no-auth";

    private final UserRepository userRepository;
    private final AppPreferenceRepository preferenceRepository;
    private final AssetRepository assetRepository;
    private final UserMapper userMapper;
    private final PasswordService passwordService;
    private final AuditService auditService;

    /* Optional build properties for dynamic version injection */
    @Autowired(required = false)
    private BuildProperties buildProperties;

    @Value("${librepm.mode:desktop}")
    private String appMode;

    @Value("${spring.application.name:LibrePM}")
    private String appName;

    /* Base path for data storage (currently relative default) */
    private final Path dataPath = Paths.get("data");

    public BootstrapServiceImpl(
            UserRepository userRepository,
            AppPreferenceRepository preferenceRepository,
            AssetRepository assetRepository,
            UserMapper userMapper,
            PasswordService passwordService,
            AuditService auditService) {
        this.userRepository = userRepository;
        this.preferenceRepository = preferenceRepository;
        this.assetRepository = assetRepository;
        this.userMapper = userMapper;
        this.passwordService = passwordService;
        this.auditService = auditService;
    }

    /**
     * Retrieves all necessary data to bootstrap the frontend application.
     * <p>
     * This includes:
     * <ul>
     *     <li>List of available local user profiles (sorted by last login)</li>
     *     <li>Global application preferences (e.g., autologin settings)</li>
     *     <li>System information (version, mode)</li>
     * </ul>
     * </p>
     *
     * @return A {@link BootstrapResponse} containing all initialization data.
     */
    @Override
    @Transactional(readOnly = true)
    public BootstrapResponse getBootstrapData() {
        log.debug("Caricamento dati bootstrap...");

        // Load all active non-ghost users (ghosts should not appear in profile picker)
        List<User> users = userRepository.findByActiveTrueAndIsGhostFalse();

        // Sort by lastLoginAt descending (most recent first), handling nulls
        users.sort((a, b) -> {
            if (a.getLastLoginAt() == null && b.getLastLoginAt() == null) return 0;
            if (a.getLastLoginAt() == null) return 1;
            if (b.getLastLoginAt() == null) return -1;
            return b.getLastLoginAt().compareTo(a.getLastLoginAt());
        });

        List<UserResponse> userResponses = users.stream()
                .map(userMapper::toResponseLight)
                .toList();

        // Load preferences
        BootstrapResponse.AppPreferencesResponse preferences = getPreferences();

        // Determine version dynamically or fallback
        String version = (buildProperties != null) ? buildProperties.getVersion() : "0.5.2-dev";

        // System info
        boolean freshInstall = users.isEmpty();
        BootstrapResponse.SystemInfoResponse systemInfo = new BootstrapResponse.SystemInfoResponse(
                version,
                appMode,
                freshInstall,
                users.size()
        );

        log.info("Bootstrap completato: {} profili, freshInstall={}, autologin={}, version={}",
                users.size(), freshInstall, preferences.autologinEnabled(), version);

        return new BootstrapResponse(userResponses, preferences, systemInfo);
    }

    /**
     * Creates a new local user profile.
     * <p>
     * Local profiles are created without a real password (using a default one)
     * and are marked as LOCAL_ONLY for sync status.
     * </p>
     *
     * @param request The profile creation request containing username and display name.
     * @return The created {@link User} entity.
     * @throws ConflictException If the username or email already exists.
     */
    @Override
    public User createLocalProfile(CreateLocalProfileRequest request) {
        log.info("Creazione profilo locale: {}", request.username());

        // Check username uniqueness
        if (userRepository.existsByUsernameIgnoreCase(request.username())) {
            throw new ConflictException("Username già esistente: " + request.username());
        }

        // Check email uniqueness (if provided)
        if (request.email() != null && !request.email().isBlank()) {
            if (userRepository.existsByEmailIgnoreCase(request.email())) {
                throw new ConflictException("Email già esistente: " + request.email());
            }
        }

        // Create user
        User user = new User();
        user.setId(UUID.randomUUID().toString());
        user.setUsername(request.username());
        user.setDisplayName(request.displayName());
        user.setEmail(request.email());
        user.setAvatarPath(request.avatarPath());
        
        // Use provided password or default
        String passwordToHash = (request.password() != null && !request.password().isBlank()) 
                ? request.password() 
                : DEFAULT_LOCAL_PASSWORD;
        user.setPasswordHash(passwordService.hash(passwordToHash));
        
        user.setActive(true);
        user.setSyncStatus(SyncStatus.LOCAL_ONLY);
        user.setLastLoginAt(Instant.now()); // Set as "just used"

        User saved = userRepository.save(user);
        log.info("Profilo locale creato: {} ({})", saved.getDisplayName(), saved.getId());

        return saved;
    }

    /**
     * Selects a profile as the active one for the current session.
     * <p>
     * Updates the {@code lastLoginAt} timestamp for the user and sets the
     * {@code lastUserId} preference.
     * </p>
     *
     * @param userId The ID of the user to select.
     * @return The selected {@link User} entity.
     * @throws ResourceNotFoundException If the user does not exist.
     * @throws ConflictException If the user is not active.
     */
    @Override
    public User selectProfile(String userId) {
        log.info("Selezione profilo: {}", userId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Profilo non trovato: " + userId));

        if (!user.isActive()) {
            throw new ConflictException("Profilo non attivo: " + userId);
        }

        // Update lastLoginAt
        user.setLastLoginAt(Instant.now());
        User saved = userRepository.save(user);

        // Update lastUserId preference
        updatePreferenceValue(AppPreference.KEY_LAST_USER_ID, userId);

        log.info("Profilo selezionato: {} ({})", saved.getDisplayName(), saved.getId());
        return saved;
    }

    @Override
    public User login(String userId, String password) {
        log.info("Tentativo login per utente: {}", userId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Profilo non trovato: " + userId));

        if (!user.isActive()) {
            throw new ConflictException("Profilo non attivo: " + userId);
        }

        // Verify password using the service (supports both bcrypt and legacy SHA-256)
        if (!passwordService.verify(password, user.getPasswordHash())) {
            log.warn("Login fallito per utente {}: password errata", userId);
            throw new UnauthorizedException("Password non valida");
        }

        // Transparent hash migration: if stored hash is legacy format, re-hash with bcrypt
        if (passwordService.needsMigration(user.getPasswordHash())) {
            String newHash = passwordService.hash(password);
            user.setPasswordHash(newHash);
            userRepository.save(user);
            log.info("Password hash migrata a bcrypt per utente: {}", userId);
            auditService.log("User", userId, AuditEvent.Action.PASSWORD_MIGRATED, userId,
                    "{\"from\":\"SHA-256\",\"to\":\"bcrypt\"}");
        }

        // Audit: successful login
        auditService.log("User", userId, AuditEvent.Action.LOGIN, userId, null);

        // If password is correct, proceed with selection logic
        return selectProfile(userId);
    }

    /**
     * Permanently deletes a local profile and its associated data.
     * <p>
     * This performs a hard delete of the user record and attempts to clean up
     * physical asset files. Cascading database constraints handle related entities.
     * </p>
     *
     * @param userId The ID of the user to delete.
     * @throws ResourceNotFoundException If the user does not exist.
     */
    @Override
    public void deleteProfile(String userId) {
        log.info("Eliminazione definitiva profilo: {}", userId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Profilo non trovato: " + userId));

        // 1. Cleanup physical asset files
        try {
            List<Asset> assets = assetRepository.findByOwnerId(userId);
            log.info("Trovati {} asset da eliminare per l'utente {}", assets.size(), userId);
            
            for (Asset asset : assets) {
                try {
                    // Construct absolute path: data/assets/{filePath}
                    Path filePath = dataPath.resolve("assets").resolve(asset.getFilePath());
                    if (Files.exists(filePath)) {
                        Files.delete(filePath);
                        log.debug("File eliminato: {}", filePath);
                    }
                    
                    // Delete thumbnail if exists
                    if (asset.getThumbnailPath() != null) {
                        Path thumbPath = dataPath.resolve("assets").resolve(asset.getThumbnailPath());
                        if (Files.exists(thumbPath)) {
                            Files.delete(thumbPath);
                        }
                    }
                } catch (IOException e) {
                    log.warn("Impossibile eliminare file fisico per asset {}: {}", asset.getId(), e.getMessage());
                    // Continue with DB record deletion anyway
                }
            }
        } catch (Exception e) {
            log.error("Errore durante pulizia asset utente: {}", e.getMessage());
        }

        // 2. Cleanup avatar (if local)
        if (user.getAvatarPath() != null && !user.getAvatarPath().startsWith("http")) {
            try {
                // Assume avatarPath is relative to data/ or assets/
                Path avatarPath = Paths.get(user.getAvatarPath());
                if (!avatarPath.isAbsolute()) {
                    avatarPath = dataPath.resolve(avatarPath);
                }
                
                if (Files.exists(avatarPath)) {
                    Files.delete(avatarPath);
                    log.debug("Avatar eliminato: {}", avatarPath);
                }
            } catch (IOException e) {
                log.warn("Impossibile eliminare avatar utente: {}", e.getMessage());
            }
        }

        // 3. Hard delete: removes record from DB
        // Foreign keys with ON DELETE CASCADE handle related data (projects, assets, sessions, teams)
        // Foreign keys with ON DELETE SET NULL handle assigned tasks
        userRepository.delete(user);

        // 4. If it was the last used user, clear the preference
        String lastUserId = getPreferenceValue(AppPreference.KEY_LAST_USER_ID, "");
        if (userId.equals(lastUserId)) {
            updatePreferenceValue(AppPreference.KEY_LAST_USER_ID, "");
            updatePreferenceValue(AppPreference.KEY_AUTOLOGIN_ENABLED, "false");
        }

        log.info("Profilo eliminato definitivamente: {} ({})", user.getDisplayName(), user.getId());
    }

    @Override
    public BootstrapResponse.AppPreferencesResponse updatePreferences(UpdateAppPreferencesRequest request) {
        log.debug("Aggiornamento preferenze: {}", request);

        if (request.lastUserId() != null) {
            // Verify user exists if not empty
            if (!request.lastUserId().isBlank()) {
                if (!userRepository.existsById(request.lastUserId())) {
                    log.warn("lastUserId non valido, verrà ignorato: {}", request.lastUserId());
                } else {
                    updatePreferenceValue(AppPreference.KEY_LAST_USER_ID, request.lastUserId());
                }
            } else {
                // Reset lastUserId
                updatePreferenceValue(AppPreference.KEY_LAST_USER_ID, "");
            }
        }

        if (request.autologinEnabled() != null) {
            updatePreferenceValue(
                    AppPreference.KEY_AUTOLOGIN_ENABLED,
                    request.autologinEnabled() ? "true" : "false"
            );
        }

        return getPreferences();
    }

    @Override
    @Transactional(readOnly = true)
    public BootstrapResponse.AppPreferencesResponse getPreferences() {
        String lastUserId = getPreferenceValue(AppPreference.KEY_LAST_USER_ID, "");
        boolean autologinEnabled = "true".equalsIgnoreCase(
                getPreferenceValue(AppPreference.KEY_AUTOLOGIN_ENABLED, "false")
        );

        return new BootstrapResponse.AppPreferencesResponse(lastUserId, autologinEnabled);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isValidForAutologin(String userId) {
        if (userId == null || userId.isBlank()) {
            return false;
        }

        return userRepository.findById(userId)
                .map(User::isActive)
                .orElse(false);
    }

    @Override
    public void autoDetectData() {
        // PRD-01: If external DB has data, LibrePM must hook into it.
        // In this basic implementation, we check if there are users.
        // If users exist but preferences are empty, try to set a default.
        
        long userCount = userRepository.count();
        if (userCount > 0) {
            String lastUserId = getPreferenceValue(AppPreference.KEY_LAST_USER_ID, "");
            if (lastUserId.isBlank()) {
                log.info("Auto-detect: trovati {} utenti ma nessun lastUserId. Cerco il più recente.", userCount);
                List<User> realUsers = userRepository.findByActiveTrueAndIsGhostFalse();
                if (!realUsers.isEmpty()) {
                    // Pick the first one (should be most recent or random)
                    User candidate = realUsers.get(0);
                    updatePreferenceValue(AppPreference.KEY_LAST_USER_ID, candidate.getId());
                    log.info("Auto-detect: impostato lastUserId={}", candidate.getId());
                }
            }
        }
    }

    // ========================================
    // Helper Methods
    // ========================================

    private String getPreferenceValue(String key, String defaultValue) {
        return preferenceRepository.findByKey(key)
                .map(AppPreference::getValue)
                .orElse(defaultValue);
    }

    private void updatePreferenceValue(String key, String value) {
        AppPreference pref = preferenceRepository.findByKey(key)
                .orElseGet(() -> {
                    AppPreference newPref = new AppPreference();
                    newPref.setKey(key);
                    return newPref;
                });

        pref.setValue(value);
        pref.setUpdatedAt(Instant.now());
        preferenceRepository.save(pref);
    }
}
