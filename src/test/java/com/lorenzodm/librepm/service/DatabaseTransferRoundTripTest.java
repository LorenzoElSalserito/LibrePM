package com.lorenzodm.librepm.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.lorenzodm.librepm.api.dto.response.DatabaseImportResponse;
import com.lorenzodm.librepm.bootstrap.PendingDbImportRunListener;
import com.lorenzodm.librepm.config.LibrePMAssetsProperties;
import com.lorenzodm.librepm.config.LibrePMDataProperties;
import com.lorenzodm.librepm.service.impl.EncryptionServiceImpl;
import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.mock.env.MockEnvironment;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Full .db round trip on real SQLite files: export from one data dir, stage the import in a fresh
 * data dir, apply it the way a backend restart does, and check the data and the safety backup.
 */
class DatabaseTransferRoundTripTest {

    @TempDir
    Path tmp;

    @Test
    void exportImportIntoFreshDataDirRestoresSameDataAndBacksUpCurrentDb() throws Exception {
        Path source = tmp.resolve("source");
        JdbcTemplate sourceJdbc = migratedDatabase(source);
        insertUserAndProject(sourceJdbc, "u-src", "Roundtrip project");
        sourceJdbc.update("INSERT INTO tasks (id, title, project_id, created_at, updated_at) VALUES ('t-1', 'Roundtrip task', 'p-Roundtrip project', '2026-01-01T00:00:00Z', '2026-01-01T00:00:00Z')");

        DatabaseTransferService sourceService = service(source, sourceJdbc);
        DatabaseTransferService.ExportedFile exported = sourceService.exportDbSnapshot();
        assertThat(exported.downloadName()).endsWith(".db");
        assertThat(sourceService.validateLibrePmDatabase(exported.path())).isNull();

        Path target = tmp.resolve("target");
        JdbcTemplate targetJdbc = migratedDatabase(target);
        insertUserAndProject(targetJdbc, "u-local", "Local only");
        DatabaseTransferService targetService = service(target, targetJdbc);

        DatabaseImportResponse staged;
        try (InputStream in = Files.newInputStream(exported.path())) {
            staged = targetService.importDbStream(in);
        }
        assertThat(staged.accepted()).as(staged.message()).isTrue();
        assertThat(staged.appliedOnNextStart()).isTrue();

        // Live DB still untouched until the backend restarts.
        assertThat(projectNames(target)).containsExactly("Local only");

        applyPendingImportLikeRestart(target);

        assertThat(projectNames(target)).containsExactly("Roundtrip project");
        assertThat(new JdbcTemplate(dataSource(target)).queryForObject(
                "SELECT title FROM tasks WHERE id = 't-1'", String.class)).isEqualTo("Roundtrip task");
        assertThat(Files.exists(target.resolve("config/db-import/pending.marker"))).isFalse();

        List<Path> backups;
        try (var files = Files.list(target.resolve("config/backup"))) {
            backups = files.filter(p -> p.toString().endsWith(".db")).toList();
        }
        assertThat(backups).hasSize(1);
        try (Connection c = DriverManager.getConnection("jdbc:sqlite:" + backups.get(0));
             Statement st = c.createStatement();
             var rs = st.executeQuery("SELECT name FROM projects")) {
            assertThat(rs.next()).isTrue();
            assertThat(rs.getString(1)).isEqualTo("Local only");
        }
    }

    @Test
    void encryptedBackupRoundTripsAndWrongPassphraseFails() throws Exception {
        Path source = tmp.resolve("enc-source");
        JdbcTemplate sourceJdbc = migratedDatabase(source);
        insertUserAndProject(sourceJdbc, "u-enc", "Encrypted project");
        byte[] db = Files.readAllBytes(service(source, sourceJdbc).exportDbSnapshot().path());

        EncryptionService encryption = new EncryptionServiceImpl();
        EncryptionService.EncryptedPayload payload = encryption.encrypt(db, "correct horse");
        assertThatThrownBy(() -> encryption.decrypt(payload, "wrong")).isInstanceOf(Exception.class);

        Path target = tmp.resolve("enc-target");
        JdbcTemplate targetJdbc = migratedDatabase(target);
        DatabaseImportResponse staged = service(target, targetJdbc)
                .importDecryptedDb(encryption.decrypt(payload, "correct horse"));
        assertThat(staged.accepted()).as(staged.message()).isTrue();

        applyPendingImportLikeRestart(target);
        assertThat(projectNames(target)).containsExactly("Encrypted project");
    }

    @Test
    void rejectsFilesThatAreNotLibrePmDatabases() throws Exception {
        Path data = tmp.resolve("validate");
        DatabaseTransferService svc = service(data, migratedDatabase(data));

        DatabaseImportResponse notSqlite = svc.importDbStream(
                new ByteArrayInputStream("hello, this is not sqlite".getBytes(StandardCharsets.UTF_8)));
        assertThat(notSqlite.accepted()).isFalse();
        assertThat(notSqlite.message()).contains("not a SQLite");

        Path foreign = tmp.resolve("foreign.db");
        try (Connection c = DriverManager.getConnection("jdbc:sqlite:" + foreign);
             Statement st = c.createStatement()) {
            st.execute("CREATE TABLE something (id INTEGER)");
        }
        DatabaseImportResponse otherApp;
        try (InputStream in = Files.newInputStream(foreign)) {
            otherApp = svc.importDbStream(in);
        }
        assertThat(otherApp.accepted()).isFalse();
        assertThat(otherApp.message()).contains("not a LibrePM database");

        Path newer = tmp.resolve("newer");
        JdbcTemplate newerJdbc = migratedDatabase(newer);
        newerJdbc.update("INSERT INTO flyway_schema_history (installed_rank, version, description, type, script, checksum, installed_by, execution_time, success) "
                + "VALUES (99999, '99999', 'future', 'SQL', 'V99999__future.sql', 0, 'test', 0, 1)");
        DatabaseImportResponse future;
        try (InputStream in = Files.newInputStream(newer.resolve("librepm.db"))) {
            future = svc.importDbStream(in);
        }
        assertThat(future.accepted()).isFalse();
        assertThat(future.message()).contains("newer LibrePM version");

        assertThat(Files.exists(data.resolve("config/db-import/pending.marker"))).isFalse();
    }

    @Test
    void rawDatabaseTransferIsDisabledOnMariaDb() {
        Path data = tmp.resolve("maria");
        LibrePMDataProperties props = new LibrePMDataProperties();
        props.setPath(data.toString());
        DatabaseTransferService svc = new DatabaseTransferService(props, new LibrePMAssetsProperties(),
                new JdbcTemplate(), new ObjectMapper(), "jdbc:mariadb://db.example:3306/librepm");

        assertThat(svc.isFileTransferSupported()).isFalse();
        assertThat(svc.status().fileTransferSupported()).isFalse();
        assertThat(svc.status().engine()).isEqualTo("mariadb");
        assertThatThrownBy(svc::exportDbSnapshot)
                .isInstanceOf(DatabaseTransferService.FileTransferUnsupportedException.class);
        assertThatThrownBy(() -> svc.importDbStream(new ByteArrayInputStream(new byte[0])))
                .isInstanceOf(DatabaseTransferService.FileTransferUnsupportedException.class);
    }

    // ------------------------------------------------------------------ helpers

    private static DriverManagerDataSource dataSource(Path dataDir) {
        return new DriverManagerDataSource("jdbc:sqlite:" + dataDir.resolve("librepm.db"));
    }

    private static JdbcTemplate migratedDatabase(Path dataDir) throws Exception {
        Files.createDirectories(dataDir);
        DriverManagerDataSource ds = dataSource(dataDir);
        Flyway.configure().dataSource(ds).locations("classpath:db/migration").load().migrate();
        return new JdbcTemplate(ds);
    }

    private static void insertUserAndProject(JdbcTemplate jdbc, String userId, String projectName) {
        jdbc.update("INSERT INTO users (id, username, password_hash, created_at, updated_at) VALUES (?, ?, 'x', '2026-01-01T00:00:00Z', '2026-01-01T00:00:00Z')",
                userId, userId);
        jdbc.update("INSERT INTO projects (id, name, owner_id, created_at, updated_at) VALUES (?, ?, ?, '2026-01-01T00:00:00Z', '2026-01-01T00:00:00Z')",
                "p-" + projectName, projectName, userId);
    }

    private static DatabaseTransferService service(Path dataDir, JdbcTemplate jdbc) {
        LibrePMDataProperties props = new LibrePMDataProperties();
        props.setPath(dataDir.toString());
        LibrePMAssetsProperties assets = new LibrePMAssetsProperties();
        assets.setStoragePath(dataDir.resolve("assets").toString());
        return new DatabaseTransferService(props, assets, jdbc, new ObjectMapper(),
                "jdbc:sqlite:" + dataDir.resolve("librepm.db"));
    }

    private static void applyPendingImportLikeRestart(Path dataDir) {
        MockEnvironment env = new MockEnvironment().withProperty("librepm.data.path", dataDir.toString());
        new PendingDbImportRunListener(null, new String[0]).environmentPrepared(null, env);
    }

    private static List<String> projectNames(Path dataDir) {
        return new JdbcTemplate(dataSource(dataDir)).queryForList("SELECT name FROM projects ORDER BY name", String.class);
    }
}
