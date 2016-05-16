package kotowari.scaffold.task;

import kotowari.scaffold.model.EntityField;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.*;

/**
 * @author kawasima
 */
public class PatternTemplateTaskTest {
    @Test
    public void test() throws Exception {
        List<EntityField> fields = Arrays.asList(
                new EntityField("id", Long.class, true),
                new EntityField("name", String.class, false),
                new EntityField("email", String.class, false)
        );
        PatternTemplateTask task = new PatternTemplateTask("ggg.ftl", "Product", fields);
        task.addMapping(String.class, "parts/textfield.ftl");
        task.addMapping(Long.class, "parts/textfield.ftl");
        task.addMapping(Integer.class, "parts/textfield.ftl");

        PathResolverMock pathResolver = new PathResolverMock();
        task.execute(pathResolver);
        System.out.println(pathResolver.getResult());
    }
}
