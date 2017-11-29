package kotowari.routing;

import enkan.collection.OptionMap;
import enkan.data.HttpRequest;
import enkan.data.HttpResponse;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

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

        assertThat(routes.generate(OptionMap.of("controller", TestController.class, "action", "index")))
                .isEqualTo("/a/b/");
        assertThat(routes.generate(OptionMap.of("controller", TestController.class, "action", "show", "id", 1)))
                .isEqualTo("/a/b/1");
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
