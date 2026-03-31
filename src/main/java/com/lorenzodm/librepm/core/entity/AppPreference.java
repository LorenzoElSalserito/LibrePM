package com.lorenzodm.librepm.core.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;
import java.util.Objects;

/**
 * Entity AppPreference - Preferenze globali dell'applicazione
 *
 * Usato per memorizzare preferenze non legate a un utente specifico:
 * - autologin_enabled: abilita login automatico all'ultimo profilo
 * - last_user_id: ID dell'ultimo utente selezionato
 *
 * Struttura key-value per flessibilità futura.
 *
 * @author Lorenzo DM
 * @since 0.3.0
 */
@Entity
@Table(name = "app_preferences")
public class AppPreference {

    /**
     * Chiave della preferenza (es. "autologin_enabled", "last_user_id")
     */
    @Id
    @Column(length = 100)
    private String key;

    /**
     * Valore della preferenza (stringa, sarà convertita nel tipo appropriato)
     */
    @Column(nullable = false, length = 500)
    private String value;

    @UpdateTimestamp
    @Column(nullable = false)
    private Instant updatedAt;

    // Costanti per chiavi predefinite
    public static final String KEY_AUTOLOGIN_ENABLED = "autologin_enabled";
    public static final String KEY_LAST_USER_ID = "last_user_id";

    // Costruttori
    public AppPreference() {
    }

    public AppPreference(String key, String value) {
        this.key = key;
        this.value = value;
    }

    // Getters & Setters
    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }

    // Helper methods

    /**
     * Interpreta il valore come booleano
     */
    public boolean getValueAsBoolean() {
        return "true".equalsIgnoreCase(value) || "1".equals(value);
    }

    /**
     * Imposta un valore booleano
     */
    public void setValueAsBoolean(boolean val) {
        this.value = val ? "true" : "false";
    }

    // equals & hashCode
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof AppPreference that)) return false;
        return Objects.equals(key, that.key);
    }

    @Override
    public int hashCode() {
        return Objects.hash(key);
    }

    @Override
    public String toString() {
        return "AppPreference{" +
                "key='" + key + '\'' +
                ", value='" + value + '\'' +
                ", updatedAt=" + updatedAt +
                '}';
    }
}