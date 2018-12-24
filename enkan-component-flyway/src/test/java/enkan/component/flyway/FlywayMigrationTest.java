package enkan.component.flyway;

import enkan.collection.OptionMap;
import enkan.component.DataSourceComponent;
import enkan.component.hikaricp.HikariCPComponent;
import enkan.system.EnkanSystem;
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
    @Test
    public void test() throws SQLException {
        EnkanSystem system = EnkanSystem.of(
                "flyway", new FlywayMigration(),
                "datasource", new HikariCPComponent(OptionMap.of(
                        "uri", "jdbc:h2:mem:test"
                ))
        ).relationships(
                component("flyway").using("datasource")
        );
        system.start();
        DataSourceComponent dataSourceComponent = system.getComponent("datasource");
        DataSource ds = dataSourceComponent.getDataSource();
        try(Connection conn = ds.getConnection();
            ResultSet rs = conn.getMetaData().getTables(null, null, "EMP", null)) {
            assertThat(rs.next()).isTrue();
            assertThat(rs.getString("TABLE_NAME")).isEqualTo("EMP");
        }

        system.stop();
    }


    @Test
    public void multipleComponents() throws SQLException {
        EnkanSystem system = EnkanSystem.of(
                "flyway", new FlywayMigration(),
                "flyway2", builder(new FlywayMigration())
                        .set(FlywayMigration::setTable, "test_versions")
                        .set(FlywayMigration::setLocation, new String[]{"classpath:db/migration2"})
                        .build(),
                "datasource", new HikariCPComponent(OptionMap.of(
                        "uri", "jdbc:h2:mem:test"
                ))
        ).relationships(
                component("flyway").using("datasource"),
                component("flyway2").using("datasource", "flyway")
        );
        system.start();
        DataSourceComponent dataSourceComponent = system.getComponent("datasource");
        DataSource ds = dataSourceComponent.getDataSource();
        try(Connection conn = ds.getConnection()) {
            try(ResultSet rs = conn.getMetaData().getTables(null, null, "EMP", null)) {
                assertThat(rs.next()).isTrue();
                assertThat(rs.getString("TABLE_NAME")).isEqualTo("EMP");
            }

            try(ResultSet rs = conn.getMetaData().getTables(null, null, "DEPT", null)) {
                assertThat(rs.next()).isTrue();
                assertThat(rs.getString("TABLE_NAME")).isEqualTo("DEPT");
            }

        }
        system.stop();

        system.setComponent("flyway", builder(new FlywayMigration())
                .set(FlywayMigration::setLocation, new String[]{
                        "classpath:db/migration",
                        "classpath:db/migration3"
                })
                .build());
        system.relationships(component("flyway").using("datasource"));

        system.start();
        dataSourceComponent = system.getComponent("datasource");
        ds = dataSourceComponent.getDataSource();
        try(Connection conn = ds.getConnection();
            ResultSet rs = conn.getMetaData().getTables(null, null, "BELONGS", null)) {
            assertThat(rs.next()).isTrue();
            assertThat(rs.getString("TABLE_NAME")).isEqualToIgnoringCase("belongs");
        }

        system.stop();
    }
}
