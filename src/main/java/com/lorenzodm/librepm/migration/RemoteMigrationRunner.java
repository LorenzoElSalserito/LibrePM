package com.lorenzodm.librepm.migration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.flyway.FlywayMigrationStrategy;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import javax.sql.DataSource;
import java.nio.file.Path;

/** Imports an explicitly selected source before Flyway and JPA initialize. */
@Configuration
@Profile("mariadb")
@ConditionalOnProperty(name = "librepm.remote-migration.enabled", havingValue = "true")
public class RemoteMigrationRunner {
    private static final Logger log = LoggerFactory.getLogger(RemoteMigrationRunner.class);
    @Bean
    FlywayMigrationStrategy remoteMigrationStrategy(DataSource target,
            @Value("${librepm.remote-migration.source}") String source) {
        return flyway -> {
            Path path = Path.of(source).toAbsolutePath().normalize();
            try {
                log.info("Starting administrative SQLite to MariaDB migration from {}", path);
                var report = new SqliteToMariaDbMigrator().migrate(path, target, false);
                log.info("Remote migration complete: {} tables, {} rows", report.tables(), report.rows());
                flyway.migrate();
            } catch (Exception error) {
                throw new IllegalStateException("Administrative MariaDB migration failed", error);
            }
        };
    }
}
