package enkan.component;

import org.flywaydb.core.Flyway;

import javax.sql.DataSource;

/**
 * @author kawasima
 */
public class FlywayMigration extends SystemComponent {
    private String[] locations;

    public FlywayMigration() {

    }

    @Override
    protected ComponentLifecycle<FlywayMigration> lifecycle() {
        return new ComponentLifecycle<FlywayMigration>() {
            @Override
            public void start(FlywayMigration component) {
                DataSourceComponent dataSourceComponent = getDependency(DataSourceComponent.class);
                DataSource dataSource = dataSourceComponent.getDataSource();
                Flyway flyway = new Flyway();

                flyway.setDataSource(dataSource);
                if (component.locations != null) {
                    flyway.setLocations(component.locations);
                }
                flyway.migrate();
            }

            @Override
            public void stop(FlywayMigration component) {

            }
        };
    }
}
