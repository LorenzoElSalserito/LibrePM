package com.lorenzodm.librepm.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.ServerSocket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

/**
 * Gestisce la porta dinamica del backend per comunicazione con Electron
 *
 * Funzionalità:
 * - Trova porta libera nel range configurato
 * - Salva porta su file JSON per lettura da Electron
 * - Include timestamp e PID per debugging
 *
 * File generato: data/config/backend.port (JSON)
 * Formato: { "port": 8234, "timestamp": "2025-01-27T...", "pid": 12345 }
 *
 * @author Lorenzo DM
 * @since 1.0.0
 */
@Component
public class PortManager {

    private static final Logger log = LoggerFactory.getLogger(PortManager.class);

    @Value("${librepm.port.config-file}")
    private String portConfigFile;

    @Value("${librepm.port.min:8000}")
    private int minPort;

    @Value("${librepm.port.max:9000}")
    private int maxPort;

    private final ObjectMapper objectMapper;

    public PortManager() {
        this.objectMapper = new ObjectMapper();
        this.objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
    }

    /**
     * Trova una porta libera nel range configurato
     *
     * @return porta libera trovata
     * @throws IOException se nessuna porta disponibile
     */
    public int findAvailablePort() throws IOException {
        // Strategia: prova prima con port 0 (OS sceglie), poi range specifico
        try (ServerSocket socket = new ServerSocket(0)) {
            int port = socket.getLocalPort();

            // Verifica se è nel range desiderato
            if (port >= minPort && port <= maxPort) {
                log.debug("✅ Porta dinamica trovata: {}", port);
                return port;
            }
        } catch (IOException e) {
            log.warn("⚠️ Impossibile usare porta dinamica OS, provo range specifico");
        }

        // Fallback: cerca nel range specificato
        for (int port = minPort; port <= maxPort; port++) {
            if (isPortAvailable(port)) {
                log.debug("✅ Porta disponibile trovata: {}", port);
                return port;
            }
        }

        throw new IOException("❌ Nessuna porta disponibile nel range " + minPort + "-" + maxPort);
    }

    /**
     * Verifica se una porta è disponibile
     */
    private boolean isPortAvailable(int port) {
        try (ServerSocket socket = new ServerSocket(port)) {
            socket.setReuseAddress(true);
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    /**
     * Salva la porta su file JSON per comunicazione con Electron
     *
     * @param port porta da salvare
     * @throws IOException se errore scrittura file
     */
    public void savePortToFile(int port) throws IOException {
        Path configPath = Paths.get(portConfigFile);

        // Assicurati che la directory esista
        Files.createDirectories(configPath.getParent());

        // Prepara dati JSON
        Map<String, Object> config = new HashMap<>();
        config.put("port", port);
        config.put("timestamp", Instant.now().toString());
        config.put("pid", ProcessHandle.current().pid());
        config.put("status", "running");

        // Scrivi file
        objectMapper.writeValue(configPath.toFile(), config);

        log.info("✅ Configurazione porta salvata: {}", configPath.toAbsolutePath());
        log.debug("📄 Contenuto: {}", config);
    }

    /**
     * Legge la porta dal file config
     * Utile per testing o diagnostics
     *
     * @return porta salvata o -1 se file non esiste
     */
    public int readPortFromFile() {
        try {
            Path configPath = Paths.get(portConfigFile);

            if (!Files.exists(configPath)) {
                log.warn("⚠️ File config porta non esiste: {}", configPath);
                return -1;
            }

            @SuppressWarnings("unchecked")
            Map<String, Object> config = objectMapper.readValue(configPath.toFile(), Map.class);

            return (int) config.getOrDefault("port", -1);

        } catch (Exception e) {
            log.error("❌ Errore lettura file porta", e);
            return -1;
        }
    }

    /**
     * Cancella il file config porta
     * Chiamato durante shutdown graceful
     */
    public void cleanupPortFile() {
        try {
            Path configPath = Paths.get(portConfigFile);

            if (Files.exists(configPath)) {
                Files.delete(configPath);
                log.info("🧹 File config porta eliminato");
            }

        } catch (Exception e) {
            log.warn("⚠️ Impossibile eliminare file porta", e);
        }
    }
}