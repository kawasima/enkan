package db.migration;

import org.flywaydb.core.api.migration.BaseJavaMigration;
import org.flywaydb.core.api.migration.Context;

import java.sql.Statement;

/**
 * @author kawasima
 */
public class V2__CreateUsers extends BaseJavaMigration {
    @Override
    public void migrate(Context context) throws Exception {
        try (Statement stmt = context.getConnection().createStatement()) {
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
