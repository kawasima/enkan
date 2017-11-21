package enkan.component.flyway;

import enkan.component.ComponentLifecycle;
import enkan.component.DataSourceComponent;
import enkan.component.SystemComponent;
import enkan.exception.UnreachableException;
import org.flywaydb.core.Flyway;

import javax.sql.DataSource;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;

/**
 * A migration component using by Flyway.
 *
 * @author kawasima
 */
public class FlywayMigration extends SystemComponent {
    private String[] locations;
    private String table = "schema_version";
    private Flyway flyway;

    public FlywayMigration() {

    }

    private boolean isMigrationAvailable() {
        return Arrays.stream(flyway.getLocations())
                .anyMatch(l-> {
                    if (l.startsWith("classpath:")) {
                        String path = l.substring("classpath:".length());
                        return Thread.currentThread().getContextClassLoader().getResource(path) != null;
                    } else if (l.startsWith("filesystem:")){
                        String path = l.substring("filesystem:".length());
                        return Files.exists(Paths.get(path));
                    } else throw new UnreachableException();
                });
    }

    @Override
    protected ComponentLifecycle<FlywayMigration> lifecycle() {
        return new ComponentLifecycle<FlywayMigration>() {
            @Override
            public void start(FlywayMigration component) {
                DataSourceComponent dataSourceComponent = getDependency(DataSourceComponent.class);
                DataSource dataSource = dataSourceComponent.getDataSource();
                component.flyway = new Flyway();
                component.flyway.setTable(table);
                component.flyway.setDataSource(dataSource);

                component.flyway.setClassLoader(Thread.currentThread().getContextClassLoader());
                if (component.locations != null) {
                    component.flyway.setLocations(component.locations);
                }

                if (isMigrationAvailable()) {
                    component.flyway.migrate();
                }
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

    public void setTable(String table) {
        this.table = table;
    }
}
