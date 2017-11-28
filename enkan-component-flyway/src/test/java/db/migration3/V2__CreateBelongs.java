package db.migration3;

import org.flywaydb.core.api.migration.jdbc.JdbcMigration;

import java.sql.Connection;
import java.sql.Statement;

public class V2__CreateBelongs implements JdbcMigration {

    @Override
    public void migrate(Connection connection) throws Exception {
        try(Statement stmt = connection.createStatement()) {
            stmt.execute("CREATE TABLE belongs("
                    + "emp_no bigint,"
                    + "dept_no bigint"
                    + ")");
        }
    }
}
