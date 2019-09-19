package kotowari.scaffold.util;

import kotowari.scaffold.model.EntityField;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.util.List;

/**
 * @author kawasima
 */
class EntitySourceAnalyzerTest {
    @Test
    void test() throws Exception {
        EntitySourceAnalyzer analyzer = new EntitySourceAnalyzer();
        List<EntityField> fields = analyzer.analyze(new File("src/test/resources/Product.java"));
        System.out.println(fields);
    }
}
