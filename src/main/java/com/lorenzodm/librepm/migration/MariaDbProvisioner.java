package com.lorenzodm.librepm.migration;

import org.flywaydb.core.Flyway;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import java.nio.file.Files;
import java.nio.file.Path;

/** Explicit offline administration command; never runs during normal application startup. */
public final class MariaDbProvisioner {
    private MariaDbProvisioner() {}
    public static SqliteToMariaDbMigrator.MigrationReport provision() throws Exception {
        String url = required("LIBREPM_DB_URL");
        if (!url.startsWith("jdbc:mariadb:")) throw new IllegalArgumentException("LIBREPM_DB_URL must use jdbc:mariadb:");
        var destination = new DriverManagerDataSource(url, required("LIBREPM_DB_USERNAME"), required("LIBREPM_DB_PASSWORD"));
        String source = System.getenv("LIBREPM_REMOTE_MIGRATION_SOURCE");
        if (source != null && !source.isBlank()) {
            return new SqliteToMariaDbMigrator().migrate(Path.of(source), destination, false);
        }
        Path schema = Files.createTempFile("librepm-schema-", ".db");
        try {
            Flyway.configure().dataSource("jdbc:sqlite:" + schema, null, null)
                    .locations("classpath:db/migration").load().migrate();
            return new SqliteToMariaDbMigrator().migrate(schema, destination, false);
        } finally { Files.deleteIfExists(schema); }
    }
    private static String required(String name) {
        String value = System.getenv(name);
        if (value == null || value.isBlank()) throw new IllegalArgumentException("Missing environment variable: " + name);
        return value;
    }
}
