package db.mariadb;

import org.flywaydb.core.api.migration.BaseJavaMigration;
import org.flywaydb.core.api.migration.Context;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 * Remote databases provisioned before 0.1.8 stored every instant as TIMESTAMP with
 * second precision: notes and messages written in the same second came back in random
 * order. Converts every TIMESTAMP column to DATETIME(6), keeping nullability and
 * defaults and dropping the implicit ON UPDATE that older MariaDB servers attach.
 */
public class V2__datetime_microsecond_precision extends BaseJavaMigration {
    @Override
    public void migrate(Context context) throws Exception {
        Connection connection = context.getConnection();
        List<String> alterations = new ArrayList<>();
        try (Statement statement = connection.createStatement();
             ResultSet columns = statement.executeQuery(
                     "SELECT table_name, column_name, is_nullable, column_default FROM information_schema.columns " +
                     "WHERE table_schema = DATABASE() AND data_type = 'timestamp' ORDER BY table_name, ordinal_position")) {
            while (columns.next()) {
                String definition = "DATETIME(6) " + ("YES".equals(columns.getString("is_nullable")) ? "NULL" : "NOT NULL");
                String defaultValue = columns.getString("column_default");
                if (defaultValue != null && defaultValue.toLowerCase().startsWith("current_timestamp")) {
                    definition += " DEFAULT CURRENT_TIMESTAMP(6)";
                } else if (defaultValue != null && !defaultValue.equalsIgnoreCase("NULL")) {
                    definition += " DEFAULT " + defaultValue;
                }
                alterations.add("ALTER TABLE `" + columns.getString("table_name") + "` MODIFY `" +
                        columns.getString("column_name") + "` " + definition);
            }
        }
        try (Statement statement = connection.createStatement()) {
            for (String alteration : alterations) statement.execute(alteration);
        }
    }
}
