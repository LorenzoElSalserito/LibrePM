package com.lorenzodm.librepm.repository;

import com.lorenzodm.librepm.core.entity.AppPreference;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository per AppPreference
 *
 * Gestisce le preferenze globali dell'applicazione.
 *
 * @author Lorenzo DM
 * @since 0.3.0
 */
@Repository
public interface AppPreferenceRepository extends JpaRepository<AppPreference, String> {

    /**
     * Trova una preferenza per chiave
     * Alias di findById per chiarezza semantica
     */
    default Optional<AppPreference> findByKey(String key) {
        return findById(key);
    }

    /**
     * Trova preferenze che iniziano con un prefisso
     * Utile per raggruppare preferenze (es. "theme_", "notification_")
     */
    List<AppPreference> findByKeyStartingWith(String prefix);

    /**
     * Verifica se una preferenza esiste
     */
    default boolean existsByKey(String key) {
        return existsById(key);
    }
}