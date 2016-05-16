package enkan.component.flyway;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * @author kawasima
 */
public class FlywayMigrationTest {
    @Test
    public void test() {
        FlywayMigration migration = new FlywayMigration();
        migration.lifecycle().start(migration);
    }
}
