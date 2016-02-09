package kotowari.routing;

import enkan.collection.OptionMap;
import enkan.data.HttpRequest;
import enkan.data.HttpResponse;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * @author kawasima
 */
public class GenerateTest {
    @Test
    public void test() {
        Routes routes = Routes.define(r -> {
            r.get("/a/b/").to(TestController.class, "index");
            r.get("/a/b/:id").to(TestController.class, "show");
        }).compile();

        assertEquals("/a/b/", routes.generate(OptionMap.of("controller", TestController.class, "action", "index")));
        assertEquals("/a/b/1", routes.generate(OptionMap.of("controller", TestController.class, "action", "show", "id", 1)));
    }

    public static class TestController {
        public HttpResponse index(HttpRequest request) {
            return null;
        }
        public HttpResponse show(HttpRequest request) {
            return null;
        }
    }
}
