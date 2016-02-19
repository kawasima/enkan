package enkan.component;

import org.flywaydb.core.Flyway;

import javax.sql.DataSource;

/**
 * @author kawasima
 */
public class FlywayMigration extends SystemComponent {
    private String[] locations;
    private Flyway flyway;

    public FlywayMigration() {

    }

    @Override
    protected ComponentLifecycle<FlywayMigration> lifecycle() {
        return new ComponentLifecycle<FlywayMigration>() {
            @Override
            public void start(FlywayMigration component) {
                DataSourceComponent dataSourceComponent = getDependency(DataSourceComponent.class);
                DataSource dataSource = dataSourceComponent.getDataSource();
                component.flyway = new Flyway();
                component.flyway.setDataSource(dataSource);

                component.flyway.setClassLoader(Thread.currentThread().getContextClassLoader());
                if (component.locations != null) {
                    component.flyway.setLocations(component.locations);
                }
                component.flyway.migrate();
            }

            @Override
            public void stop(FlywayMigration component) {
                component.flyway = null;
            }
        };
    }

    public void setLocation(String[] locations) {
        this.locations = locations;
    }
}
