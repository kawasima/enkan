package db.migration3;

import org.flywaydb.core.api.migration.BaseJavaMigration;
import org.flywaydb.core.api.migration.Context;

import java.sql.Statement;

public class V2__CreateBelongs extends BaseJavaMigration {
    @Override
    public void migrate(Context context) throws Exception {
        try(Statement stmt = context.getConnection().createStatement()) {
            stmt.execute("CREATE TABLE belongs("
                    + "emp_no bigint,"
                    + "dept_no bigint"
                    + ")");
        }
    }
}
