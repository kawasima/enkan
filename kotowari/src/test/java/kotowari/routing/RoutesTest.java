package kotowari.routing;

import enkan.collection.OptionMap;
import kotowari.routing.controller.ExampleController;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

/**
 * @author kawasima
 */
public class RoutesTest {
    @Test
    public void simple() {
        Routes routes = Routes.define(r -> r.get("/").to(ExampleController.class, "method1")).compile();

        OptionMap m = routes.recognizePath("/", "GET");
        assertThat(m.get("controller")).isEqualTo(ExampleController.class);
        assertThat(m.get("action")).isEqualTo("method1");
    }

    @Test
    public void scope() {
        Routes routes = Routes.define(r -> {
            r.get("/home6").to(ExampleController.class, "method6");
            r.scope("/admin", admin -> admin.get("/list").to(ExampleController.class, "method1"));
        }).compile();

        OptionMap m = routes.recognizePath("/admin/list", "GET");
        assertThat(m.get("controller")).isEqualTo(ExampleController.class);
        assertThat(m.get("action")).isEqualTo("method1");

        m = routes.recognizePath("/home6", "GET");
        assertThat(m.get("controller")).isEqualTo(ExampleController.class);
        assertThat(m.get("action")).isEqualTo("method6");
    }

    @Test
    public void nestedScope() {
        Routes routes = Routes.define(r ->
                r.scope("/admin", admin ->
                        admin.scope("/user", user ->
                                user.get("/list").to(ExampleController.class, "method1")))
        ).compile();

        OptionMap m = routes.recognizePath("/admin/user/list", "GET");
        assertThat(m.get("controller")).isEqualTo(ExampleController.class);
        assertThat(m.get("action")).isEqualTo("method1");
    }
}
