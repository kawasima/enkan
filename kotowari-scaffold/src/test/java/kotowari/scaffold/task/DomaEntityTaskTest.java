package kotowari.scaffold.task;

import org.h2.jdbcx.JdbcDataSource;
import org.junit.jupiter.api.Test;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * @author kawasima
 */
class DomaEntityTaskTest {
    void createTable(DataSource ds) throws SQLException {
        try (Connection conn = ds.getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.executeUpdate("CREATE TABLE product (" +
                    "id BIGINT NOT NULL," +
                    "name VARCHAR(255)" +
                    ")");
            conn.commit();
        }
    }

    @Test
    void test() throws Exception {
        JdbcDataSource ds = new JdbcDataSource();
        ds.setURL("jdbc:h2:mem:test;DB_CLOSE_DELAY=-1");
        createTable(ds);
        DomaEntityTask task = new DomaEntityTask("example/entity", "product", ds);
        PathResolverMock resolver = new PathResolverMock();
        task.execute(resolver);
    }
}
