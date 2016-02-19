package db.migration;

import org.flywaydb.core.api.migration.jdbc.JdbcMigration;

import java.sql.Connection;
import java.sql.Statement;

/**
 * @author kawasima
 */
public class V2__CreateUsers implements JdbcMigration {
    @Override
    public void migrate(Connection connection) throws Exception {
        try (Statement stmt = connection.createStatement()) {
            stmt.execute("CREATE TABLE users " +
                    "(id VARCHAR(20) PRIMARY KEY," +
                    " first_name VARCHAR(30)," +
                    " last_name VARCHAR(30)," +
                    " email VARCHAR(30)," +
                    " admin BOOLEAN," +
                    " last_login TIMESTAMP," +
                    " is_active BOOLEAN," +
                    " pass VARCHAR(100))");
        }
    }
}
