package com.lorenzodm.librepm.migration;

import javax.sql.DataSource;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.*;
import java.util.*;
import java.util.regex.Pattern;

/**
 * Creates a MariaDB schema from a LibrePM SQLite database and copies every row.
 * The target operation is transactional and refuses non-empty databases unless
 * replacement is explicitly requested.
 */
public final class SqliteToMariaDbMigrator {
    private static final Pattern SAFE_NAME = Pattern.compile("[A-Za-z_][A-Za-z0-9_]*");

    public MigrationReport migrate(Path sqliteFile, DataSource target, boolean replace) throws SQLException {
        if (!Files.isRegularFile(sqliteFile)) {
            throw new IllegalArgumentException("SQLite source does not exist: " + sqliteFile);
        }
        try (Connection source = DriverManager.getConnection("jdbc:sqlite:" + sqliteFile.toAbsolutePath());
             Connection destination = target.getConnection()) {
            requireMariaDb(destination);
            if (replace) throw new SQLException("Replacing a non-empty MariaDB target is intentionally unsupported; use a new empty database");
            destination.setAutoCommit(false);
            List<String> createdTables = new ArrayList<>();
            try {
                execute(destination, "SET FOREIGN_KEY_CHECKS=0");
                List<String> tables = sourceTables(source);
                requireSafeTarget(destination);

                Map<String, Long> copied = new LinkedHashMap<>();
                for (String table : tables) {
                    createTable(source, destination, table);
                    createdTables.add(table);
                }
                for (String table : tables) copied.put(table, copyRows(source, destination, table));
                for (String table : tables) createIndexes(source, destination, table);
                for (String table : tables) createForeignKeys(source, destination, table);
                execute(destination, "SET FOREIGN_KEY_CHECKS=1");
                verify(source, destination, tables, copied);
                destination.commit();
                long rows = copied.values().stream().mapToLong(Long::longValue).sum();
                return new MigrationReport(tables.size(), rows, Collections.unmodifiableMap(copied));
            } catch (Exception error) {
                destination.rollback();
                try {
                    execute(destination, "SET FOREIGN_KEY_CHECKS=0");
                    for (String table : createdTables.reversed()) execute(destination, "DROP TABLE IF EXISTS " + quote(table));
                    execute(destination, "SET FOREIGN_KEY_CHECKS=1");
                } catch (SQLException cleanupError) {
                    error.addSuppressed(cleanupError);
                }
                if (error instanceof SQLException sql) throw sql;
                throw new SQLException("SQLite to MariaDB migration failed", error);
            }
        }
    }

    private void requireMariaDb(Connection connection) throws SQLException {
        String product = connection.getMetaData().getDatabaseProductName();
        if (!product.toLowerCase(Locale.ROOT).contains("mariadb")) {
            throw new SQLException("Target must be MariaDB, found: " + product);
        }
    }

    private List<String> sourceTables(Connection source) throws SQLException {
        List<String> tables = new ArrayList<>();
        try (PreparedStatement statement = source.prepareStatement(
                "SELECT name FROM sqlite_master WHERE type='table' AND name NOT LIKE 'sqlite_%' ORDER BY name");
             ResultSet rows = statement.executeQuery()) {
            while (rows.next()) tables.add(safe(rows.getString(1)));
        }
        if (tables.isEmpty()) throw new SQLException("SQLite source has no application tables");
        return tables;
    }

    private void requireSafeTarget(Connection target) throws SQLException {
        Set<String> existing = targetTables(target);
        if (!existing.isEmpty()) throw new SQLException("MariaDB target is not empty; use a new empty database");
    }

    private Set<String> targetTables(Connection target) throws SQLException {
        Set<String> tables = new LinkedHashSet<>();
        try (ResultSet rows = target.getMetaData().getTables(target.getCatalog(), null, "%", new String[]{"TABLE"})) {
            while (rows.next()) tables.add(safe(rows.getString("TABLE_NAME")));
        }
        return tables;
    }

    private void createTable(Connection source, Connection target, String table) throws SQLException {
        List<Column> columns = columns(source, table);
        Set<String> indexed = indexedColumns(source, table);
        List<String> definitions = new ArrayList<>();
        List<String> primary = new ArrayList<>();
        for (Column column : columns) {
            if (column.primaryKeyOrder > 0) primary.add(column.name);
            boolean key = column.primaryKeyOrder > 0 || indexed.contains(column.name);
            String definition = quote(column.name) + " " + mariaType(column, key);
            if (column.notNull) definition += " NOT NULL";
            String defaultValue = mariaDefault(column.defaultValue);
            if (defaultValue != null) definition += " DEFAULT " + defaultValue;
            if (column.autoIncrement) definition += " AUTO_INCREMENT";
            definitions.add(definition);
        }
        primary.sort(Comparator.comparingInt(name -> columns.stream().filter(c -> c.name.equals(name)).findFirst().orElseThrow().primaryKeyOrder));
        if (!primary.isEmpty()) definitions.add("PRIMARY KEY (" + primary.stream().map(SqliteToMariaDbMigrator::quote).reduce((a, b) -> a + "," + b).orElseThrow() + ")");
        execute(target, "CREATE TABLE " + quote(table) + " (" + String.join(",", definitions) + ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci");
    }

    private List<Column> columns(Connection source, String table) throws SQLException {
        List<Column> result = new ArrayList<>();
        String tableSql = "";
        try (PreparedStatement statement = source.prepareStatement("SELECT sql FROM sqlite_master WHERE type='table' AND name=?")) {
            statement.setString(1, table);
            try (ResultSet row = statement.executeQuery()) { if (row.next()) tableSql = Objects.toString(row.getString(1), ""); }
        }
        try (Statement statement = source.createStatement(); ResultSet rows = statement.executeQuery("PRAGMA table_info(" + quoteSqlite(table) + ")")) {
            while (rows.next()) {
                String name = safe(rows.getString("name"));
                String type = Objects.toString(rows.getString("type"), "TEXT").toUpperCase(Locale.ROOT);
                int pk = rows.getInt("pk");
                boolean auto = pk > 0 && type.contains("INT") && tableSql.toUpperCase(Locale.ROOT).contains("AUTOINCREMENT");
                result.add(new Column(name, type, rows.getInt("notnull") != 0, rows.getString("dflt_value"), pk, auto));
            }
        }
        return result;
    }

    private Set<String> indexedColumns(Connection source, String table) throws SQLException {
        Set<String> result = new HashSet<>();
        try (Statement statement = source.createStatement(); ResultSet indexes = statement.executeQuery("PRAGMA index_list(" + quoteSqlite(table) + ")")) {
            while (indexes.next()) {
                String index = safe(indexes.getString("name"));
                try (Statement detail = source.createStatement(); ResultSet columns = detail.executeQuery("PRAGMA index_info(" + quoteSqlite(index) + ")")) {
                    while (columns.next()) result.add(safe(columns.getString("name")));
                }
            }
        }
        try (Statement statement = source.createStatement(); ResultSet keys = statement.executeQuery("PRAGMA foreign_key_list(" + quoteSqlite(table) + ")")) {
            while (keys.next()) result.add(safe(keys.getString("from")));
        }
        return result;
    }

    private String mariaType(Column column, boolean indexed) {
        String type = column.type;
        if (column.autoIncrement) return "BIGINT";
        if (type.contains("BOOL")) return "BOOLEAN";
        if (type.contains("INT")) return "BIGINT";
        if (type.contains("REAL") || type.contains("FLOA") || type.contains("DOUB")) return "DOUBLE";
        if (type.contains("DEC") || type.contains("NUM")) return "DECIMAL(38,10)";
        if (type.contains("BLOB")) return "LONGBLOB";
        if (type.startsWith("DATE") && !type.startsWith("DATETIME")) return "DATE";
        if (type.startsWith("TIMESTAMP") || type.startsWith("DATETIME")) return "TIMESTAMP";
        if (type.startsWith("TIME")) return "TIME";
        var sizedText = Pattern.compile("(?:VAR)?CHAR\\s*\\((\\d+)\\)").matcher(type);
        if (sizedText.find()) return "VARCHAR(" + Math.min(Integer.parseInt(sizedText.group(1)), indexed ? 191 : 16383) + ")";
        // 191 utf8mb4 characters remain indexable even on conservative InnoDB
        // key limits and cover LibrePM UUIDs, statuses, usernames and paths.
        return indexed ? "VARCHAR(191)" : "LONGTEXT";
    }

    private String mariaDefault(String value) {
        if (value == null) return null;
        String normalized = value.trim();
        String lower = normalized.toLowerCase(Locale.ROOT);
        if (lower.contains("datetime('now')") || lower.contains("strftime(")) return "CURRENT_TIMESTAMP";
        if (normalized.equalsIgnoreCase("TRUE")) return "1";
        if (normalized.equalsIgnoreCase("FALSE")) return "0";
        return normalized;
    }

    private long copyRows(Connection source, Connection target, String table) throws SQLException {
        List<Column> columns = columns(source, table);
        String names = columns.stream().map(c -> quote(c.name)).reduce((a, b) -> a + "," + b).orElseThrow();
        String placeholders = String.join(",", Collections.nCopies(columns.size(), "?"));
        long copied = 0;
        try (Statement read = source.createStatement(); ResultSet rows = read.executeQuery("SELECT * FROM " + quoteSqlite(table));
             PreparedStatement write = target.prepareStatement("INSERT INTO " + quote(table) + " (" + names + ") VALUES (" + placeholders + ")")) {
            while (rows.next()) {
                for (int i = 1; i <= columns.size(); i++) {
                    Object value = rows.getObject(i);
                    if (value instanceof byte[] bytes) write.setBytes(i, bytes); else write.setObject(i, value);
                }
                write.addBatch();
                copied++;
                if (copied % 500 == 0) write.executeBatch();
            }
            write.executeBatch();
        }
        return copied;
    }

    private void createIndexes(Connection source, Connection target, String table) throws SQLException {
        Set<String> created = new HashSet<>();
        try (Statement statement = source.createStatement(); ResultSet indexes = statement.executeQuery("PRAGMA index_list(" + quoteSqlite(table) + ")")) {
            while (indexes.next()) {
                String origin = indexes.getString("origin");
                if ("pk".equalsIgnoreCase(origin)) continue;
                String sourceName = safe(indexes.getString("name"));
                List<String> columns = new ArrayList<>();
                try (Statement detail = source.createStatement(); ResultSet rows = detail.executeQuery("PRAGMA index_info(" + quoteSqlite(sourceName) + ")")) {
                    while (rows.next()) columns.add(safe(rows.getString("name")));
                }
                if (columns.isEmpty()) continue;
                String signature = indexes.getInt("unique") + ":" + String.join(",", columns);
                if (!created.add(signature)) continue;
                boolean unique = indexes.getInt("unique") != 0;
                String rawName = "idx_" + table + "_" + String.join("_", columns) + (unique ? "_u" : "");
                String indexName = safe(rawName.substring(0, Math.min(60, rawName.length())));
                execute(target, "CREATE " + (unique ? "UNIQUE " : "") + "INDEX " + quote(indexName) + " ON " + quote(table) + " (" + columns.stream().map(SqliteToMariaDbMigrator::quote).reduce((a, b) -> a + "," + b).orElseThrow() + ")");
            }
        }
    }

    private void createForeignKeys(Connection source, Connection target, String table) throws SQLException {
        try (Statement statement = source.createStatement(); ResultSet keys = statement.executeQuery("PRAGMA foreign_key_list(" + quoteSqlite(table) + ")")) {
            while (keys.next()) {
                String from = safe(keys.getString("from"));
                String parent = safe(keys.getString("table"));
                String to = safe(keys.getString("to"));
                String constraint = safe(("fk_" + table + "_" + from).substring(0, Math.min(60, ("fk_" + table + "_" + from).length())));
                execute(target, "ALTER TABLE " + quote(table) + " ADD CONSTRAINT " + quote(constraint) + " FOREIGN KEY (" + quote(from) + ") REFERENCES " + quote(parent) + " (" + quote(to) + ") ON UPDATE " + action(keys.getString("on_update")) + " ON DELETE " + action(keys.getString("on_delete")));
            }
        }
    }

    private String action(String value) {
        return switch (Objects.toString(value, "NO ACTION").toUpperCase(Locale.ROOT)) {
            case "CASCADE" -> "CASCADE";
            case "SET NULL" -> "SET NULL";
            case "RESTRICT" -> "RESTRICT";
            default -> "NO ACTION";
        };
    }

    private void verify(Connection source, Connection target, List<String> tables, Map<String, Long> copied) throws SQLException {
        if (!targetTables(target).containsAll(tables)) throw new SQLException("Target schema misses one or more tables");
        for (String table : tables) {
            long sourceCount = count(source, table);
            long targetCount = count(target, table);
            if (sourceCount != targetCount || sourceCount != copied.get(table)) {
                throw new SQLException("Row count mismatch for " + table + ": SQLite=" + sourceCount + ", MariaDB=" + targetCount);
            }
            int sourceColumns = columns(source, table).size();
            int targetColumns = 0;
            try (ResultSet rows = target.getMetaData().getColumns(target.getCatalog(), null, table, "%")) { while (rows.next()) targetColumns++; }
            if (sourceColumns != targetColumns) throw new SQLException("Column count mismatch for " + table);
        }
    }

    private long count(Connection connection, String table) throws SQLException {
        try (Statement statement = connection.createStatement(); ResultSet row = statement.executeQuery("SELECT COUNT(*) FROM " + (connection.getMetaData().getDatabaseProductName().toLowerCase(Locale.ROOT).contains("sqlite") ? quoteSqlite(table) : quote(table)))) {
            row.next(); return row.getLong(1);
        }
    }

    private void execute(Connection connection, String sql) throws SQLException {
        try (Statement statement = connection.createStatement()) { statement.execute(sql); }
    }

    private static String safe(String name) {
        if (name == null || !SAFE_NAME.matcher(name).matches()) throw new IllegalArgumentException("Unsafe SQL identifier: " + name);
        return name;
    }
    private static String quote(String name) { return "`" + safe(name) + "`"; }
    private static String quoteSqlite(String name) { return "\"" + safe(name) + "\""; }

    private record Column(String name, String type, boolean notNull, String defaultValue, int primaryKeyOrder, boolean autoIncrement) { }
    public record MigrationReport(int tables, long rows, Map<String, Long> rowsByTable) { }
}
