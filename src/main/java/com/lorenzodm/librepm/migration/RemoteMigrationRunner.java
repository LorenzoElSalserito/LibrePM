package com.lorenzodm.librepm.migration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.nio.file.Path;

/** Administrative startup migration. Not exposed to the desktop renderer. */
@Component
@ConditionalOnProperty(name = "librepm.remote-migration.enabled", havingValue = "true")
public class RemoteMigrationRunner implements ApplicationRunner {
    private static final Logger log = LoggerFactory.getLogger(RemoteMigrationRunner.class);
    private final DataSource target;
    private final Path source;

    public RemoteMigrationRunner(DataSource target,
                                 @Value("${librepm.remote-migration.source}") String source) {
        this.target = target;
        this.source = Path.of(source).toAbsolutePath().normalize();
    }

    @Override
    public void run(ApplicationArguments args) throws Exception {
        log.info("Starting administrative SQLite to MariaDB migration from {}", source);
        var report = new SqliteToMariaDbMigrator().migrate(source, target, false);
        log.info("Remote migration complete: {} tables, {} rows", report.tables(), report.rows());
    }
}
