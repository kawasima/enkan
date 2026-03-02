package enkan.component.hikaricp;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import enkan.collection.OptionMap;
import enkan.exception.MisconfigurationException;
import enkan.system.EnkanSystem;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class HikariCPComponentTest {
    private EnkanSystem system;
    private HikariCPComponent component;

    @BeforeEach
    void setUp() {
        component = new HikariCPComponent(OptionMap.of("uri", "jdbc:h2:mem:hikaritest;DB_CLOSE_DELAY=-1"));
        system = EnkanSystem.of("hikari", component);
    }

    @AfterEach
    void tearDown() {
        if (system != null) {
            system.stop();
        }
    }

    // ---- start / getDataSource ----

    @Test
    void dataSourceIsAvailableAfterStart() {
        system.start();
        DataSource ds = component.getDataSource();
        assertThat(ds).isNotNull().isInstanceOf(HikariDataSource.class);
    }

    @Test
    void dataSourceIsNullBeforeStart() {
        assertThat(component.getDataSource()).isNull();
    }

    @Test
    void invalidConfigThrowsMisconfigurationException() {
        HikariCPComponent bad = new HikariCPComponent(); // no JDBC URL
        EnkanSystem badSystem = EnkanSystem.of("hikari", bad);
        assertThatThrownBy(badSystem::start).isInstanceOf(MisconfigurationException.class);
    }

    // ---- stop ----

    @Test
    void stopClosesDataSource() {
        system.start();
        HikariDataSource ds = (HikariDataSource) component.getDataSource();
        assertThat(ds.isClosed()).isFalse();

        system.stop();
        system = null; // prevent @AfterEach from calling stop() again

        assertThat(ds.isClosed()).isTrue();
        assertThat(component.getDataSource()).isNull();
    }

    // ---- restart ----

    @Test
    void restartCreatesNewDataSource() {
        system.start();
        HikariDataSource first = (HikariDataSource) component.getDataSource();

        system.stop();
        system.start();

        HikariDataSource second = (HikariDataSource) component.getDataSource();
        assertThat(first.isClosed()).isTrue();
        assertThat(second.isClosed()).isFalse();
        assertThat(second).isNotSameAs(first);
    }

    // ---- OptionMap constructor ----

    @Test
    void optionMapUsernameIsApplied() {
        HikariCPComponent c = new HikariCPComponent(OptionMap.of(
                "uri", "jdbc:h2:mem:usertest",
                "username", "sa",
                "password", ""
        ));
        EnkanSystem s = EnkanSystem.of("hikari", c);
        s.start();
        try {
            assertThat(((HikariDataSource) c.getDataSource()).getUsername()).isEqualTo("sa");
        } finally {
            s.stop();
        }
    }

    @Test
    void optionMapMaxPoolSizeIsApplied() {
        HikariCPComponent c = new HikariCPComponent(OptionMap.of(
                "uri", "jdbc:h2:mem:pooltest",
                "maxPoolSize", 3
        ));
        EnkanSystem s = EnkanSystem.of("hikari", c);
        s.start();
        try {
            assertThat(((HikariDataSource) c.getDataSource()).getMaximumPoolSize()).isEqualTo(3);
        } finally {
            s.stop();
        }
    }

    @Test
    void optionMapPoolNameIsApplied() {
        HikariCPComponent c = new HikariCPComponent(OptionMap.of(
                "uri", "jdbc:h2:mem:poolnametest",
                "poolName", "MyPool"
        ));
        EnkanSystem s = EnkanSystem.of("hikari", c);
        s.start();
        try {
            assertThat(((HikariDataSource) c.getDataSource()).getPoolName()).isEqualTo("MyPool");
        } finally {
            s.stop();
        }
    }

    @Test
    void optionMapAutoCommitIsApplied() throws SQLException {
        HikariCPComponent c = new HikariCPComponent(OptionMap.of(
                "uri", "jdbc:h2:mem:autocommittest",
                "autoCommit?", false
        ));
        EnkanSystem s = EnkanSystem.of("hikari", c);
        s.start();
        try (Connection conn = c.getDataSource().getConnection()) {
            assertThat(conn.getAutoCommit()).isFalse();
        } finally {
            s.stop();
        }
    }

    // ---- setConfig overrides OptionMap ----

    @Test
    void setConfigOverridesOptionMap() {
        HikariConfig newConfig = new HikariConfig();
        newConfig.setJdbcUrl("jdbc:h2:mem:overridetest");
        component.setConfig(newConfig);
        system.start();

        assertThat(((HikariDataSource) component.getDataSource()).getJdbcUrl())
                .isEqualTo("jdbc:h2:mem:overridetest");
    }

    // ---- toString ----

    @Test
    void toStringContainsJdbcUrl() {
        assertThat(component.toString()).contains("jdbc:h2:mem:hikaritest");
    }
}
