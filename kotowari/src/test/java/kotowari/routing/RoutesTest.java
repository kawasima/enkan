package kotowari.routing;

import enkan.collection.OptionMap;
import enkan.util.CodecUtils;
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

    @Test
    public void recognizeUtf8Dynamic() {
        Routes routes = Routes.define(r -> {
            r.get("/:val1").to(ExampleController.class, "method1");
            r.get("/*glob").to(ExampleController.class, "method2");
        }).compile();

        OptionMap m = routes.recognizePath("/" + CodecUtils.urlEncode("あいう"), "GET");
        assertThat(m.get("controller")).isEqualTo(ExampleController.class);
        assertThat(m.get("action")).isEqualTo("method1");
        assertThat(m.get("val1")).isEqualTo("あいう");
    }

    @Test
    public void recognizeUtf8Path() {
        Routes routes = Routes.define(r -> {
            r.get("/:val1").to(ExampleController.class, "method1");
            r.get("/*glob").to(ExampleController.class, "method2");
        }).compile();

        OptionMap m = routes.recognizePath("/" + CodecUtils.urlEncode("あいう") +
                "/" + CodecUtils.urlEncode("かきく"), "GET");
        assertThat(m.get("controller")).isEqualTo(ExampleController.class);
        assertThat(m.get("action")).isEqualTo("method2");
        assertThat(m.get("glob")).isEqualTo("あいう/かきく");
    }

    @Test
    public void invalidUtf8() {
        Routes routes = Routes.define(r -> r.get("/:val1").to(ExampleController.class, "method1")).compile();

        OptionMap m = routes.recognizePath("/%RR%01%01", "GET");
        assertThat(m.get("controller")).isEqualTo(ExampleController.class);
        assertThat(m.get("action")).isEqualTo("method1");
        assertThat(m.get("val1")).isEqualTo("%RR%01%01");
    }

}
