package enkan.component.hikaricp;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import enkan.exception.MisconfigurationException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.sql.DataSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class HikariCPComponentTest {
    private HikariCPComponent component;

    @BeforeEach
    void setUp() {
        component = new HikariCPComponent();
    }

    @AfterEach
    void tearDown() {
        component.lifecycle().stop(component);
    }

    @Test
    void dataSourceIsConfiguredCorrectly() {
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl("jdbc:h2:mem:test");
        config.setUsername("sa");
        config.setPassword("");

        component.setConfig(config);
        component.lifecycle().start(component);

        DataSource dataSource = component.getDataSource();

        assertThat(dataSource).isNotNull();
        assertThat(dataSource).isInstanceOf(HikariDataSource.class);
    }

    @Test
    void dataSourceThrowsExceptionWhenNotConfigured() {
        assertThatThrownBy(() -> component.lifecycle().start(component)).isInstanceOf(MisconfigurationException.class);
    }

    @Test
    void closeClosesDataSource() {
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl("jdbc:h2:mem:test");
        config.setUsername("sa");
        config.setPassword("");

        component.setConfig(config);
        component.lifecycle().start(component);

        DataSource dataSource = component.getDataSource();

        assertThat(((HikariDataSource) dataSource).isClosed()).isFalse();
    }
}