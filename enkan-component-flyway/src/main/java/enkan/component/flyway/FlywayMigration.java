package enkan.component.flyway;

import enkan.component.ComponentLifecycle;
import enkan.component.DataSourceComponent;
import enkan.component.SystemComponent;
import enkan.exception.UnreachableException;
import org.flywaydb.core.Flyway;
import org.flywaydb.core.api.configuration.FluentConfiguration;

import javax.sql.DataSource;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;

/**
 * A migration component using by Flyway.
 *
 * @author kawasima
 */
public class FlywayMigration extends SystemComponent<FlywayMigration> {
    private String[] locations;
    private boolean cleanBeforeMigration = false;
    private String table = "schema_version";
    private Flyway flyway;

    public FlywayMigration() {

    }

    private boolean isMigrationAvailable() {
        return Arrays.stream(flyway.getConfiguration().getLocations())
                .anyMatch(l-> {
                    if (l.isClassPath()) {
                        return Thread.currentThread().getContextClassLoader().getResource(l.getPath()) != null;
                    } else if (l.isFileSystem()){
                        return Files.exists(Paths.get(l.getPath()));
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
                FluentConfiguration configuration = Flyway.configure(Thread.currentThread().getContextClassLoader())
                        .table(table)
                        .baselineOnMigrate(true)
                        .baselineVersion("0")
                        .dataSource(dataSource);

                if (component.locations != null) {
                    configuration.locations(component.locations);
                }

                if (component.cleanBeforeMigration) {
                    configuration.cleanDisabled(false);
                }
                component.flyway = configuration.load();

                if (isMigrationAvailable()) {
                    if (component.cleanBeforeMigration) {
                        component.flyway.clean();
                    }
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

    public void setCleanBeforeMigration(boolean cleanBeforeMigration) {
        this.cleanBeforeMigration = cleanBeforeMigration;
    }
}
