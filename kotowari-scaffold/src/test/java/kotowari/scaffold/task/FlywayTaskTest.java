package kotowari.scaffold.task;

import org.junit.jupiter.api.Test;

/**
 * @author kawasima
 */
class FlywayTaskTest {
    @Test
    void test() throws Exception {
        FlywayTask task = new FlywayTask("", "User", "sql");
        PathResolverMock resolver = new PathResolverMock();
        task.execute(resolver);
        System.out.println(resolver.getResult());
    }
}
