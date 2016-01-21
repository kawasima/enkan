package kotowari.routing;

import kotowari.routing.controller.ExampleController;
import org.junit.Test;

/**
 * @author kawasima
 */
public class RoutesTest {
    @Test
    public void test() {
        Routes routes = Routes.define(r -> {
            r.get("/").to(ExampleController.class, "method1");
        }).compile();


        System.out.println(routes.recognizePath("/", "GET"));
        System.out.println(routes);
    }
}
