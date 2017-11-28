package db.migration2;

import org.flywaydb.core.api.migration.jdbc.JdbcMigration;

import java.sql.Connection;
import java.sql.Statement;

public class V1__CreateDept implements JdbcMigration {
    @Override
    public void migrate(Connection connection) throws Exception {
        try(Statement stmt = connection.createStatement()) {
            stmt.execute("CREATE TABLE dept("
                    + "dept_no identity,"
                    + "name VARCHAR(100)"
                    + ")");
        }
    }
}
