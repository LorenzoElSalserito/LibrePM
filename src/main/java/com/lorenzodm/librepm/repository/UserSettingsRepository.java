package com.lorenzodm.librepm.repository;

import com.lorenzodm.librepm.core.entity.UserSettings;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repository per entity UserSettings
 * 
 * @author Lorenzo DM
 * @since 0.2.0
 */
@Repository
public interface UserSettingsRepository extends JpaRepository<UserSettings, String> {

    /**
     * Trova settings per user ID
     */
    Optional<UserSettings> findByUserId(String userId);

    /**
     * Verifica se esistono settings per un utente
     */
    boolean existsByUserId(String userId);
}
