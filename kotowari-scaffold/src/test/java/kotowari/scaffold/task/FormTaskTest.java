package kotowari.scaffold.task;

import kotowari.scaffold.model.EntityField;
import org.junit.Test;

import java.util.Arrays;

/**
 * @author kawasima
 */
public class FormTaskTest {
    @Test
    public void test() throws Exception {
        FormTask task = new FormTask("", "Product", Arrays.asList(
                new EntityField("id",    Long.class, true),
                new EntityField("name",  String.class, false),
                new EntityField("email", String.class, false)
        ));
        PathResolverMock pathResolver = new PathResolverMock();
        task.execute(pathResolver);
    }

}
