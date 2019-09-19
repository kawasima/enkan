package kotowari.scaffold.task;

import kotowari.scaffold.model.EntityField;
import org.junit.jupiter.api.Test;

import java.util.Arrays;

/**
 * @author kawasima
 */
class FormTaskTest {
    @Test
    void test() throws Exception {
        FormTask task = new FormTask("", "Product", Arrays.asList(
                new EntityField("id",    Long.class, true),
                new EntityField("name",  String.class, false),
                new EntityField("email", String.class, false)
        ));
        PathResolverMock pathResolver = new PathResolverMock();
        task.execute(pathResolver);
    }

}
