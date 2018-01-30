package enkan.component.doma2;

import enkan.component.ComponentLifecycle;
import enkan.component.ComponentRelationship;
import enkan.component.DataSourceComponent;
import enkan.system.EnkanSystem;
import org.h2.jdbcx.JdbcDataSource;
import org.junit.jupiter.api.Test;
import org.seasar.doma.jdbc.Config;
import org.seasar.doma.jdbc.ConfigSupport;
import org.seasar.doma.jdbc.tx.EnkanLocalTransactionDataSource;
import org.seasar.doma.jdbc.tx.LocalTransactionManager;
import org.seasar.doma.jdbc.tx.TransactionManager;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import static org.assertj.core.api.Assertions.assertThat;

public class DomaProviderTest {
    @Test
    public void test() throws Exception {
        JdbcDataSource ds = new JdbcDataSource();
        ds.setURL("jdbc:h2:mem:test;AUTOCOMMIT=FALSE;DB_CLOSE_DELAY=-1");

        EnkanSystem system = EnkanSystem.of("doma", new DomaProvider(),
                "datasource", new DataSourceComponent() {
                    @Override
                    public DataSource getDataSource() {
                        return ds;
                    }

                    @Override
                    protected ComponentLifecycle<DataSourceComponent> lifecycle() {
                        return new ComponentLifecycle<DataSourceComponent>() {
                            @Override
                            public void start(DataSourceComponent component) {
                            }

                            @Override
                            public void stop(DataSourceComponent dataSourceComponent) {
                            }
                        };
                    }
                }).relationships(
                ComponentRelationship.component("doma").using("datasource")
        );
        system.start();
        try {
            DomaProvider provider = system.getComponent("doma");
            Config config = provider.getDefaultConfig();
            assertThat(config.getDataSource()).isInstanceOf(EnkanLocalTransactionDataSource.class);

            TransactionManager tm = new LocalTransactionManager(((EnkanLocalTransactionDataSource) config.getDataSource()).getLocalTransaction(ConfigSupport.defaultJdbcLogger));
            tm.required(() -> {
                try (Connection conn = config.getDataSource().getConnection()) {
                    Statement stmt = conn.createStatement();
                    stmt.execute("CREATE TABLE books(id INTEGER, name VARCHAR(10))");
                    conn.commit();
                } catch (SQLException e) {
                    throw new RuntimeException(e);
                }
            });

            try {
                tm.required(() -> {
                    try (Connection conn = config.getDataSource().getConnection()) {
                        Statement stmt = conn.createStatement();
                        stmt.execute("INSERT INTO books(id, name) VALUES (1, 'ABC')");
                        stmt.execute("INSERT INTO books(id, name) VALUES (2, 'ABCDEFGHIJASDS')");
                        conn.commit();
                    } catch (SQLException e) {
                        throw new RuntimeException(e);
                    }
                });
            } catch(Exception ignore) {
                // ignore
            }

            try (Connection conn = config.getDataSource().getConnection()) {
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery("SELECT count(*) FROM books");
                assertThat(rs.next()).isTrue();
                assertThat(rs.getInt(1)).isEqualTo(0);
            }
        } finally {
            system.stop();
        }
    }
}
