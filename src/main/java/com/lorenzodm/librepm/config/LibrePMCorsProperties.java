package com.lorenzodm.librepm.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.ArrayList;
import java.util.List;

/**
 * Proprietà CORS LibrePM.
 * Bind sicuro da application.yml, con fallback che evita crash se manca configurazione.
 */
@ConfigurationProperties(prefix = "librepm.cors")
public class LibrePMCorsProperties {

    /**
     * Lista origini consentite. Supporta pattern (es: http://localhost:* , file://*).
     */
    private List<String> allowedOrigins = new ArrayList<>(List.of("http://localhost:5173"));

    /**
     * Se true abilita credenziali (cookie/session). Utile per OAuth2 login.
     */
    private boolean allowCredentials = true;

    /**
     * Cache preflight in secondi.
     */
    private long maxAgeSeconds = 3600;

    public List<String> getAllowedOrigins() {
        return allowedOrigins;
    }

    public void setAllowedOrigins(List<String> allowedOrigins) {
        this.allowedOrigins = (allowedOrigins == null) ? new ArrayList<>() : allowedOrigins;
    }

    public boolean isAllowCredentials() {
        return allowCredentials;
    }

    public void setAllowCredentials(boolean allowCredentials) {
        this.allowCredentials = allowCredentials;
    }

    public long getMaxAgeSeconds() {
        return maxAgeSeconds;
    }

    public void setMaxAgeSeconds(long maxAgeSeconds) {
        this.maxAgeSeconds = maxAgeSeconds;
    }
}
