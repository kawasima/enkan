package kotowari.middleware;

import enkan.Middleware;
import enkan.MiddlewareChain;
import enkan.chain.DefaultMiddlewareChain;
import enkan.component.ComponentLifecycle;
import enkan.component.SystemComponent;
import enkan.data.DefaultHttpRequest;
import enkan.data.HttpRequest;
import enkan.data.HttpResponse;
import enkan.exception.MisconfigurationException;
import enkan.security.UserPrincipal;
import enkan.system.inject.ComponentInjector;
import enkan.util.Predicates;
import kotowari.component.TemplateEngine;
import kotowari.data.TemplatedHttpResponse;
import kotowari.io.LazyRenderInputStream;
import kotowari.io.LazyRenderer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;

import static org.assertj.core.api.Assertions.*;

class RenderTemplateMiddlewareTest {
    private RenderTemplateMiddleware sut;
    @SuppressWarnings("unchecked")
    private static Function<List, Object> HAS_ANY_PERMISSIONS = arguments -> {
        if (arguments.size() >= 2) {
            Object principal = arguments.get(0);
            if (principal instanceof UserPrincipal) {
                return arguments.subList(1, arguments.size())
                        .stream()
                        .anyMatch(p -> ((UserPrincipal) principal).hasPermission(Objects.toString(p)));
            } else {
                throw new MisconfigurationException("kotowari.HAS_PERMISSION_FIRST_ARG", "hasAnyPermission");
            }
        } else {
            throw new MisconfigurationException("kotowari.HAS_ANY_PERMISSION_WRONG_ARGS");
        }
    };

    @BeforeEach
    void setup() {
        sut = new RenderTemplateMiddleware();
    }

    @Test
    void hasAnyPermissions() {
        Object ret = HAS_ANY_PERMISSIONS.apply(Arrays.asList(
                new UserPrincipal() {
                    @Override
                    public boolean hasPermission(String permission) {
                        return Objects.equals("PERM1", permission);
                    }

                    @Override
                    public String getName() {
                        return "test";
                    }
                }
        , "PERM1"));
        assertThat(ret).isInstanceOf(Boolean.class).isEqualTo(true);
    }

    @Test
    void render() {
        final TemplatedHttpResponse response = TemplatedHttpResponse.create("template1");
        response.setBody("hello");
        sut.render(response);
        assertThat(response.getBodyAsString()).isEqualTo("hello");
    }

    @Test
    void handle() {
        final HttpRequest request = new DefaultHttpRequest();
        Map<String, SystemComponent> components = new HashMap<>();
        TemplateEngine templateEngine = new TemplateEngine() {
            @Override
            protected ComponentLifecycle lifecycle() {
                return null;
            }

            @Override
            public HttpResponse render(String name, Object... keyOrVals) {
                final TemplatedHttpResponse response = TemplatedHttpResponse.create("template1");
                response.setBody(new LazyRenderInputStream(() ->
                        new ByteArrayInputStream("hello".getBytes())));

                return response;
            }

            @Override
            public Object createFunction(Function func) {
                return null;
            }
        };
        components.put("templateEngine", templateEngine);
        ComponentInjector injector = new ComponentInjector(components);
        injector.inject(sut);

        final HttpResponse res = sut.handle(request, new DefaultMiddlewareChain(Predicates.ANY, "null",
                (Middleware<HttpRequest, HttpResponse, HttpRequest, HttpResponse>) (request1, chain) ->
                        templateEngine.render("template1")));

        assertThat(res.getBodyAsString()).isEqualTo("hello");
    }

}
