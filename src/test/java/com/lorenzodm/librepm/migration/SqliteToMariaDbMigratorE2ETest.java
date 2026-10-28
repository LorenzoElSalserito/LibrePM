package com.lorenzodm.librepm.migration;

import ch.vorburger.mariadb4j.DB;
import ch.vorburger.mariadb4j.DBConfigurationBuilder;
import com.lorenzodm.librepm.LibrePMApplication;
import com.lorenzodm.librepm.repository.ProjectRepository;
import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;

import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.LinkedHashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class SqliteToMariaDbMigratorE2ETest {
    @TempDir
    Path temporaryDirectory;

    @Test
    void migratesCompleteLibrePmSchemaAndDataToRealMariaDb() throws Exception {
        Path sqlite = temporaryDirectory.resolve("librepm.db");
        String sqliteUrl = "jdbc:sqlite:" + sqlite;
        Flyway.configure().dataSource(sqliteUrl, null, null).locations("classpath:db/migration").load().migrate();

        try (Connection source = DriverManager.getConnection(sqliteUrl); Statement statement = source.createStatement()) {
            statement.executeUpdate("INSERT INTO users (id, username, password_hash, active, created_at, updated_at) VALUES ('e2e-user', 'maria-e2e', 'hash', 1, '2026-08-30T12:00:00', '2026-08-30T12:00:00')");
            statement.executeUpdate("INSERT INTO projects (id, name, created_at, updated_at, owner_id) VALUES ('e2e-project', 'Remote project', '2026-08-30T12:00:00', '2026-08-30T12:00:00', 'e2e-user')");
            statement.executeUpdate("INSERT INTO tasks (id, title, status, priority, created_at, updated_at, project_id) VALUES ('e2e-task', 'Migrated task', 'TODO', 'MED', '2026-08-30T12:00:00', '2026-08-30T12:00:00', 'e2e-project')");
        }

        DBConfigurationBuilder configuration = DBConfigurationBuilder.newBuilder();
        configuration.setPort(0);
        configuration.setBaseDir(temporaryDirectory.resolve("mariadb-base").toFile());
        configuration.setDataDir(temporaryDirectory.resolve("mariadb-data").toFile());
        DB database = DB.newEmbeddedDB(configuration.build());
        try {
            database.start();
            String targetUrl = configuration.getURL("librepm_e2e") + "?createDatabaseIfNotExist=true";
            DriverManagerDataSource target = new DriverManagerDataSource(targetUrl, "root", "");

            var report = new SqliteToMariaDbMigrator().migrate(sqlite, target, false);
            assertTrue(report.tables() >= 60, "all LibrePM tables must be migrated");
            assertEquals(sourceCounts(sqliteUrl), targetCounts(targetUrl));

            try (Connection connection = DriverManager.getConnection(targetUrl, "root", ""); Statement statement = connection.createStatement()) {
                try (ResultSet row = statement.executeQuery("SELECT title FROM tasks WHERE id='e2e-task'")) {
                    assertTrue(row.next());
                    assertEquals("Migrated task", row.getString(1));
                }
                statement.executeUpdate("UPDATE tasks SET title='Collaborative edit' WHERE id='e2e-task'");
                try (ResultSet row = statement.executeQuery("SELECT title FROM tasks WHERE id='e2e-task'")) {
                    assertTrue(row.next());
                    assertEquals("Collaborative edit", row.getString(1));
                }
            }

            // Simulate a pre-messaging remote schema: vendor migration must upgrade it.
            try (Connection connection = DriverManager.getConnection(targetUrl, "root", ""); Statement statement = connection.createStatement()) {
                statement.executeUpdate("DROP TABLE project_messages");
                // ...and the second-precision TIMESTAMP columns written by earlier provisioners.
                statement.executeUpdate("ALTER TABLE tasks MODIFY created_at TIMESTAMP NOT NULL");
            }

            try (ConfigurableApplicationContext application = new SpringApplicationBuilder(LibrePMApplication.class)
                    .profiles("mariadb")
                    .properties(
                            "spring.jpa.hibernate.ddl-auto=none",
                            "spring.flyway.enabled=true",
                            "spring.task.scheduling.enabled=false",
                            "server.port=0",
                            "librepm.remote-migration.enabled=false")
                    .run(
                            "--spring.datasource.url=" + targetUrl,
                            "--spring.datasource.username=root",
                            "--spring.datasource.password=",
                            "--logging.file.name=" + temporaryDirectory.resolve("remote.log"))) {
                var project = application.getBean(ProjectRepository.class).findById("e2e-project").orElseThrow();
                assertEquals("Remote project", project.getName());
                var messages = application.getBean(com.lorenzodm.librepm.repository.ProjectMessageRepository.class);
                var sent = messages.saveAndFlush(new com.lorenzodm.librepm.core.entity.ProjectMessage(
                        "e2e-project", "e2e-user", "MariaDB persistent message"));
                assertNotNull(sent.getCreatedAt());
                assertEquals("MariaDB persistent message", messages.findById(sent.getId()).orElseThrow().getContent());
            }

            try (Connection connection = DriverManager.getConnection(targetUrl, "root", ""); Statement statement = connection.createStatement()) {
                try (ResultSet legacy = statement.executeQuery("SELECT COUNT(*) FROM information_schema.columns WHERE table_schema = DATABASE() AND data_type = 'timestamp'")) {
                    legacy.next();
                    assertEquals(0, legacy.getInt(1), "every instant column must be upgraded to DATETIME(6)");
                }
                try (ResultSet row = statement.executeQuery("SELECT column_type FROM information_schema.columns WHERE table_schema = DATABASE() AND table_name = 'notes' AND column_name = 'created_at'")) {
                    assertTrue(row.next());
                    assertEquals("datetime(6)", row.getString(1));
                }
                try (ResultSet row = statement.executeQuery("SELECT created_at FROM tasks WHERE id='e2e-task'")) {
                    assertTrue(row.next());
                    assertEquals("2026-08-30 12:00:00.0", row.getTimestamp(1).toString());
                }
            }

            // Legacy administrative startup import must precede vendor Flyway migrations.
            String startupUrl = configuration.getURL("librepm_startup_e2e") + "?createDatabaseIfNotExist=true";
            try (ConfigurableApplicationContext application = new SpringApplicationBuilder(LibrePMApplication.class)
                    .profiles("mariadb")
                    .run("--spring.datasource.url=" + startupUrl,
                            "--spring.datasource.username=root", "--spring.datasource.password=",
                            "--librepm.remote-migration.enabled=true", "--librepm.remote-migration.source=" + sqlite,
                            "--server.port=0", "--logging.file.name=" + temporaryDirectory.resolve("startup.log"))) {
                assertEquals("Remote project", application.getBean(ProjectRepository.class).findById("e2e-project").orElseThrow().getName());
            }

            SQLException collision = assertThrows(SQLException.class,
                    () -> new SqliteToMariaDbMigrator().migrate(sqlite, target, false));
            assertTrue(collision.getMessage().contains("target is not empty"));
        } finally {
            database.stop();
        }
    }

    private Map<String, Long> sourceCounts(String url) throws Exception {
        try (Connection connection = DriverManager.getConnection(url)) {
            Map<String, Long> counts = new LinkedHashMap<>();
            try (Statement statement = connection.createStatement(); ResultSet tables = statement.executeQuery("SELECT name FROM sqlite_master WHERE type='table' AND name NOT LIKE 'sqlite_%' ORDER BY name")) {
                while (tables.next()) counts.put(tables.getString(1), count(connection, "\"" + tables.getString(1) + "\""));
            }
            return counts;
        }
    }

    private Map<String, Long> targetCounts(String url) throws Exception {
        try (Connection connection = DriverManager.getConnection(url, "root", "")) {
            Map<String, Long> counts = new LinkedHashMap<>();
            try (ResultSet tables = connection.getMetaData().getTables(connection.getCatalog(), null, "%", new String[]{"TABLE"})) {
                while (tables.next()) counts.put(tables.getString("TABLE_NAME"), count(connection, "`" + tables.getString("TABLE_NAME") + "`"));
            }
            return counts;
        }
    }

    private long count(Connection connection, String table) throws Exception {
        try (Statement statement = connection.createStatement(); ResultSet row = statement.executeQuery("SELECT COUNT(*) FROM " + table)) {
            row.next();
            return row.getLong(1);
        }
    }
}
