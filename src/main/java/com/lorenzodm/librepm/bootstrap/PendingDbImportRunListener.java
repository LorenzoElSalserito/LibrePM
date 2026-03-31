package com.lorenzodm.librepm.bootstrap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ConfigurableBootstrapContext;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.SpringApplicationRunListener;
import org.springframework.core.env.ConfigurableEnvironment;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.*;
import java.time.Instant;
import java.time.format.DateTimeFormatter;

public class PendingDbImportRunListener implements SpringApplicationRunListener {

    private static final Logger log = LoggerFactory.getLogger(PendingDbImportRunListener.class);
    private static final String SQLITE_HEADER = "SQLite format 3\u0000";

    public PendingDbImportRunListener(SpringApplication application, String[] args) {
        // richiesto da Spring
    }

    /**
     * Spring Boot 3.4: firma corretta del metodo environmentPrepared
     */
    @Override
    public void environmentPrepared(ConfigurableBootstrapContext bootstrapContext, ConfigurableEnvironment environment) {
        String dataPathStr = environment.getProperty("librepm.data.path", "./data");

        Path dataPath = Paths.get(dataPathStr).toAbsolutePath().normalize();
        Path configDir = dataPath.resolve("config");
        Path importDir = configDir.resolve("db-import");
        Path marker = importDir.resolve("pending.marker");
        Path pending = importDir.resolve("pending-librepm.db");
        Path db = dataPath.resolve("librepm.db");

        if (!Files.exists(marker) || !Files.exists(pending)) {
            return;
        }

        try {
            if (!isSqliteFile(pending)) {
                log.warn("⚠️ Pending DB import trovato ma non è SQLite valido. Elimino pending.");
                Files.deleteIfExists(pending);
                Files.deleteIfExists(marker);
                return;
            }

            Files.createDirectories(configDir.resolve("backup"));

            String ts = DateTimeFormatter.ISO_INSTANT.format(Instant.now()).replace(":", "-");
            Path backup = configDir.resolve("backup").resolve("librepm-before-import-" + ts + ".db");

            if (Files.exists(db)) {
                Files.copy(db, backup, StandardCopyOption.REPLACE_EXISTING);
                log.info("🧷 Backup DB creato: {}", backup);
            } else {
                log.info("ℹ️ Nessun DB precedente trovato, import iniziale.");
            }

            safeMove(pending, db);

            Files.deleteIfExists(marker);

            log.info("✅ Import DB applicato all'avvio: {}", db);

        } catch (Exception e) {
            log.error("❌ Errore applicazione import DB all'avvio: {}", e.getMessage(), e);
        }
    }

    private static void safeMove(Path src, Path dst) throws IOException {
        Files.createDirectories(dst.getParent());
        try {
            Files.move(src, dst, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
        } catch (AtomicMoveNotSupportedException ex) {
            Files.move(src, dst, StandardCopyOption.REPLACE_EXISTING);
        }
    }

    private static boolean isSqliteFile(Path path) {
        try (InputStream is = Files.newInputStream(path, StandardOpenOption.READ)) {
            byte[] header = new byte[SQLITE_HEADER.length()];
            int read = is.read(header);
            if (read != SQLITE_HEADER.length()) return false;
            return SQLITE_HEADER.equals(new String(header));
        } catch (IOException e) {
            return false;
        }
    }
}
