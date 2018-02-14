package kotowari.routing;

import enkan.collection.Headers;
import enkan.collection.OptionMap;
import enkan.data.ContentNegotiable;
import enkan.data.DefaultHttpRequest;
import enkan.data.HttpRequest;
import enkan.util.CodecUtils;
import enkan.util.MixinUtils;
import kotowari.routing.controller.ExampleController;
import org.junit.jupiter.api.Test;

import javax.ws.rs.core.MediaType;

import static enkan.util.BeanBuilder.*;
import static org.assertj.core.api.Assertions.*;

/**
 * @author kawasima
 */
public class RoutesTest {
    @Test
    public void simple() {
        Routes routes = Routes.define(r -> r.get("/").to(ExampleController.class, "method1")).compile();

        OptionMap m = routes.recognizePath(builder(new DefaultHttpRequest())
                .set(HttpRequest::setUri, "/")
                .set(HttpRequest::setRequestMethod, "GET")
                .build());
        assertThat(m.get("controller")).isEqualTo(ExampleController.class);
        assertThat(m.get("action")).isEqualTo("method1");
    }

    @Test
    public void scope() {
        Routes routes = Routes.define(r -> {
            r.get("/home6").to(ExampleController.class, "method6");
            r.scope("/admin", admin -> admin.get("/list").to(ExampleController.class, "method1"));
        }).compile();

        OptionMap m = routes.recognizePath(builder(new DefaultHttpRequest())
                .set(HttpRequest::setUri, "/admin/list")
                .set(HttpRequest::setRequestMethod, "GET")
                .build());
        assertThat(m.get("controller")).isEqualTo(ExampleController.class);
        assertThat(m.get("action")).isEqualTo("method1");

        m = routes.recognizePath(builder(new DefaultHttpRequest())
                .set(HttpRequest::setUri, "/home6")
                .set(HttpRequest::setRequestMethod, "GET")
                .build());
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

        OptionMap m = routes.recognizePath(builder(new DefaultHttpRequest())
                .set(HttpRequest::setUri, "/admin/user/list")
                .set(HttpRequest::setRequestMethod, "GET")
                .build());
        assertThat(m.get("controller")).isEqualTo(ExampleController.class);
        assertThat(m.get("action")).isEqualTo("method1");
    }

    @Test
    public void recognizeUtf8Dynamic() {
        Routes routes = Routes.define(r -> {
            r.get("/:val1").to(ExampleController.class, "method1");
            r.get("/*glob").to(ExampleController.class, "method2");
        }).compile();

        OptionMap m = routes.recognizePath(builder(new DefaultHttpRequest())
                .set(HttpRequest::setUri, "/" + CodecUtils.urlEncode("あいう"))
                .set(HttpRequest::setRequestMethod, "GET")
                .build());
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

        OptionMap m = routes.recognizePath(builder(new DefaultHttpRequest())
                .set(HttpRequest::setUri, "/" + CodecUtils.urlEncode("あいう") +
                        "/" + CodecUtils.urlEncode("かきく"))
                .set(HttpRequest::setRequestMethod, "GET")
                .build());
        assertThat(m.get("controller")).isEqualTo(ExampleController.class);
        assertThat(m.get("action")).isEqualTo("method2");
        assertThat(m.get("glob")).isEqualTo("あいう/かきく");
    }

    @Test
    public void invalidUtf8() {
        Routes routes = Routes.define(r -> r.get("/:val1").to(ExampleController.class, "method1")).compile();

        OptionMap m = routes.recognizePath(builder(new DefaultHttpRequest())
                .set(HttpRequest::setUri, "/%RR%01%01")
                .set(HttpRequest::setRequestMethod, "GET")
                .build());
        assertThat(m.get("controller")).isEqualTo(ExampleController.class);
        assertThat(m.get("action")).isEqualTo("method1");
        assertThat(m.get("val1")).isEqualTo("%RR%01%01");
    }

    @Test
    public void consume() {
        Routes routes = Routes.define(r ->
                r.get("/api").consumes(MediaType.APPLICATION_JSON_TYPE)
                        .to(ExampleController.class, "method1")
        ).compile();

        OptionMap notFound = routes.recognizePath(builder(new DefaultHttpRequest())
                .set(HttpRequest::setUri, "/api")
                .set(HttpRequest::setRequestMethod, "GET")
                .build());
        assertThat(notFound).isEmpty();

        OptionMap found = routes.recognizePath(builder(new DefaultHttpRequest())
                .set(HttpRequest::setUri, "/api")
                .set(HttpRequest::setHeaders, Headers.of("content-type", "application/json"))
                .set(HttpRequest::setRequestMethod, "GET")
                .build());
        assertThat(found.get("action")).isEqualTo("method1");
    }

    @Test
    public void produceNoAccept() {
        Routes routes = Routes.define(r ->
                r.get("/api").produces(MediaType.APPLICATION_JSON_TYPE)
                        .to(ExampleController.class, "method1")
        ).compile();

        OptionMap found = routes.recognizePath(builder(new DefaultHttpRequest())
                .set(HttpRequest::setUri, "/api")
                .set(HttpRequest::setRequestMethod, "GET")
                .build());
        assertThat(found.get("action"))
                .as("If there is no accept header, all requests can be produced.")
                .isEqualTo("method1");
    }

    @Test
    public void cannotProduce() {
        Routes routes = Routes.define(r ->
                r.get("/api").produces(MediaType.APPLICATION_JSON_TYPE)
                        .to(ExampleController.class, "method1")
        ).compile();

        HttpRequest request = builder(new DefaultHttpRequest())
                .set(HttpRequest::setUri, "/api")
                .set(HttpRequest::setRequestMethod, "GET")
                .build();
        request = MixinUtils.mixin(request, ContentNegotiable.class);
        ((ContentNegotiable) request).setMediaType(MediaType.APPLICATION_XML_TYPE);
        OptionMap notFound = routes.recognizePath(request);
        assertThat(notFound).isEmpty();
    }

    @Test
    public void produceWildcard() {
        Routes routes = Routes.define(r ->
                r.get("/api").produces(MediaType.WILDCARD_TYPE)
                        .to(ExampleController.class, "method1")
        ).compile();

        HttpRequest request = builder(new DefaultHttpRequest())
                .set(HttpRequest::setUri, "/api")
                .set(HttpRequest::setRequestMethod, "GET")
                .build();
        request = MixinUtils.mixin(request, ContentNegotiable.class);
        ((ContentNegotiable) request).setMediaType(MediaType.APPLICATION_XML_TYPE);
        OptionMap found = routes.recognizePath(request);
        assertThat(found).isNotEmpty();
        assertThat(found.get("action")).isEqualTo("method1");
    }
}
