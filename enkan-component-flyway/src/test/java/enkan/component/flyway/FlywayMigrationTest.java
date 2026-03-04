package enkan.component.flyway;

import enkan.collection.OptionMap;
import enkan.component.DataSourceComponent;
import enkan.component.hikaricp.HikariCPComponent;
import enkan.system.EnkanSystem;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import javax.sql.DataSource;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;

import static enkan.component.ComponentRelationship.*;
import static enkan.util.BeanBuilder.builder;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author kawasima
 */
public class FlywayMigrationTest {
    private EnkanSystem system;

    private String jdbcUrl(String dbName) {
        return "jdbc:h2:mem:" + dbName + ";DB_CLOSE_DELAY=-1";
    }

    @AfterEach
    void tearDown() {
        if (system != null) {
            system.stop();
            system = null;
        }
    }

    @Test
    void migratesEmpTable() throws SQLException {
        system = EnkanSystem.of(
                "flyway", new FlywayMigration(),
                "datasource", new HikariCPComponent(OptionMap.of("uri", jdbcUrl("migratesEmpTable")))
        ).relationships(component("flyway").using("datasource"));
        system.start();

        DataSourceComponent<HikariCPComponent> dataSourceComponent = system.getComponent("datasource");
        DataSource ds = dataSourceComponent.getDataSource();
        try (Connection conn = ds.getConnection();
             ResultSet rs = conn.getMetaData().getTables(null, null, "EMP", null)) {
            assertThat(rs.next()).isTrue();
            assertThat(rs.getString("TABLE_NAME")).isEqualTo("EMP");
        }
    }

    @Test
    void multipleComponentsMigrateIndependentTables() throws SQLException {
        system = EnkanSystem.of(
                "flyway", new FlywayMigration(),
                "flyway2", builder(new FlywayMigration())
                        .set(FlywayMigration::setTable, "test_versions")
                        .set(FlywayMigration::setLocations, new String[]{"classpath:db/migration2"})
                        .build(),
                "datasource", new HikariCPComponent(OptionMap.of("uri", jdbcUrl("multipleComponents")))
        ).relationships(
                component("flyway").using("datasource"),
                component("flyway2").using("datasource", "flyway")
        );
        system.start();

        DataSourceComponent<HikariCPComponent> dataSourceComponent = system.getComponent("datasource");
        DataSource ds = dataSourceComponent.getDataSource();
        try (Connection conn = ds.getConnection()) {
            try (ResultSet rs = conn.getMetaData().getTables(null, null, "EMP", null)) {
                assertThat(rs.next()).isTrue();
                assertThat(rs.getString("TABLE_NAME")).isEqualTo("EMP");
            }
            try (ResultSet rs = conn.getMetaData().getTables(null, null, "DEPT", null)) {
                assertThat(rs.next()).isTrue();
                assertThat(rs.getString("TABLE_NAME")).isEqualTo("DEPT");
            }
            // custom table name for schema history (Flyway quotes the name so it stays lowercase)
            try (ResultSet rs = conn.getMetaData().getTables(null, null, "test_versions", null)) {
                assertThat(rs.next()).isTrue();
            }
        }
    }

    @Test
    void additionalLocationsAppliedOnStart() throws SQLException {
        system = EnkanSystem.of(
                "flyway", builder(new FlywayMigration())
                        .set(FlywayMigration::setLocations, new String[]{
                                "classpath:db/migration",
                                "classpath:db/migration3"
                        })
                        .build(),
                "datasource", new HikariCPComponent(OptionMap.of("uri", jdbcUrl("additionalLocations")))
        ).relationships(component("flyway").using("datasource"));
        system.start();

        DataSourceComponent<HikariCPComponent> dataSourceComponent = system.getComponent("datasource");
        DataSource ds = dataSourceComponent.getDataSource();
        try (Connection conn = ds.getConnection();
             ResultSet rs = conn.getMetaData().getTables(null, null, "BELONGS", null)) {
            assertThat(rs.next()).isTrue();
            assertThat(rs.getString("TABLE_NAME")).isEqualToIgnoringCase("belongs");
        }
    }

    @Test
    void nonExistentLocationSkipsMigration() throws SQLException {
        system = EnkanSystem.of(
                "flyway", builder(new FlywayMigration())
                        .set(FlywayMigration::setLocations, new String[]{"classpath:db/nonexistent"})
                        .build(),
                "datasource", new HikariCPComponent(OptionMap.of("uri", jdbcUrl("nonExistentLocation")))
        ).relationships(component("flyway").using("datasource"));
        system.start();

        DataSourceComponent<HikariCPComponent> dataSourceComponent = system.getComponent("datasource");
        DataSource ds = dataSourceComponent.getDataSource();
        // EMP table should NOT be created since migration was skipped
        try (Connection conn = ds.getConnection();
             ResultSet rs = conn.getMetaData().getTables(null, null, "EMP", null)) {
            assertThat(rs.next()).isFalse();
        }
    }
}
