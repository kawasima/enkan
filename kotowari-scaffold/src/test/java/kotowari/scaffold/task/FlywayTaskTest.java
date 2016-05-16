package kotowari.scaffold.task;

import org.junit.Test;

/**
 * @author kawasima
 */
public class FlywayTaskTest {
    @Test
    public void test() throws Exception {
        FlywayTask task = new FlywayTask("", "User", "sql");
        PathResolverMock resolver = new PathResolverMock();
        task.execute(resolver);
        System.out.println(resolver.getResult());
    }
}
