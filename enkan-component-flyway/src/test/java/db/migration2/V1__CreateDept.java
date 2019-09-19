package db.migration2;

import org.flywaydb.core.api.migration.BaseJavaMigration;
import org.flywaydb.core.api.migration.Context;

import java.sql.Statement;

public class V1__CreateDept extends BaseJavaMigration {
    @Override
    public void migrate(Context context) throws Exception {
        try(Statement stmt = context.getConnection().createStatement()) {
            stmt.execute("CREATE TABLE dept("
                    + "dept_no identity,"
                    + "name VARCHAR(100)"
                    + ")");
        }
    }
}
