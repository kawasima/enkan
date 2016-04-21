package db.migration;

import org.flywaydb.core.api.migration.jdbc.JdbcMigration;

import java.sql.Connection;
import java.sql.Statement;

/**
 * @author kawasima
 */
public class V3__CreateGuestbook implements JdbcMigration {
    @Override
    public void migrate(Connection connection) throws Exception {
        try (Statement stmt = connection.createStatement()) {
            stmt.execute("CREATE TABLE guestbook " +
                    "(id IDENTITY PRIMARY KEY," +
                    " name VARCHAR(30)," +
                    " message VARCHAR(200)," +
                    " postedat TIMESTAMP)");
        }
    }
}
