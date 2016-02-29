package db.migration;

import org.flywaydb.core.api.migration.jdbc.JdbcMigration;

import java.sql.Connection;
import java.sql.Statement;

/**
 * @author kawasima
 */
public class V1__CreateCustomer implements JdbcMigration {
    @Override
    public void migrate(Connection connection) throws Exception {
        try (Statement stmt = connection.createStatement()) {
            stmt.execute("CREATE TABLE customer("
                    + "id identity primary key, "
                    + "name varchar(255), "
                    + "password varchar(255), "
                    + "email varchar(255), "
                    + "gender varchar(1), "
                    + "birthday DATE"
                    + ")");
        }
    }
}
