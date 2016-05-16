package kotowari.routing;

import enkan.collection.OptionMap;
import kotowari.routing.controller.ExampleController;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * @author kawasima
 */
public class RoutesTest {
    @Test
    public void simple() {
        Routes routes = Routes.define(r -> r.get("/").to(ExampleController.class, "method1")).compile();

        OptionMap m = routes.recognizePath("/", "GET");
        assertEquals(ExampleController.class, m.get("controller"));
        assertEquals("method1", m.get("action"));
    }

    @Test
    public void scope() {
        Routes routes = Routes.define(r -> {
            r.get("/home6").to(ExampleController.class, "method6");
            r.scope("/admin", admin -> {
                admin.get("/list").to(ExampleController.class, "method1");
            });
        }).compile();

        OptionMap m = routes.recognizePath("/admin/list", "GET");
        assertEquals(ExampleController.class, m.get("controller"));
        assertEquals("method1", m.get("action"));

        m = routes.recognizePath("/home6", "GET");
        assertEquals(ExampleController.class, m.get("controller"));
        assertEquals("method6", m.get("action"));

    }

    @Test
    public void nestedScope() {
        Routes routes = Routes.define(r ->
                r.scope("/admin", admin ->
                        admin.scope("/user", user ->
                                user.get("/list").to(ExampleController.class, "method1")))
        ).compile();

        OptionMap m = routes.recognizePath("/admin/user/list", "GET");
        assertEquals(ExampleController.class, m.get("controller"));
        assertEquals("method1", m.get("action"));


    }
}
