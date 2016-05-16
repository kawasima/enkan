package kotowari.scaffold.util;

import kotowari.scaffold.model.EntityField;
import org.junit.Test;

import java.io.File;
import java.util.List;

/**
 * @author kawasima
 */
public class EntitySourceAnalyzerTest {
    @Test
    public void test() throws Exception {
        EntitySourceAnalyzer analyzer = new EntitySourceAnalyzer();
        List<EntityField> fields = analyzer.analyze(new File("src/test/resources/Product.java"));
        System.out.println(fields);
    }
}
