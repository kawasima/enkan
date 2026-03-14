package kotowari.example.graalvm;

import tools.jackson.databind.DeserializationFeature;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.json.JsonMapper;
import enkan.Application;
import enkan.application.WebApplication;
import enkan.config.ApplicationFactory;
import enkan.data.HttpRequest;
import enkan.data.HttpResponse;
import enkan.middleware.*;
import enkan.system.inject.ComponentInjector;
import jakarta.ws.rs.ext.MessageBodyWriter;
import kotowari.example.graalvm.controller.TodoController;
import kotowari.example.graalvm.jaxrs.JsonBodyReader;
import kotowari.example.graalvm.jaxrs.JsonBodyWriter;
import kotowari.graalvm.NativeControllerInvokerMiddleware;
import kotowari.graalvm.RouteRegistry;
import kotowari.middleware.*;
import kotowari.middleware.serdes.ToStringBodyWriter;
import kotowari.routing.Routes;

import static enkan.util.BeanBuilder.builder;

public class NativeApplicationFactory implements ApplicationFactory<HttpRequest, HttpResponse> {

    /**
     * Returns the compiled routes for this application.
     * Called by {@code KotowariFeature} at native-image build time via the
     * {@code kotowari.routes.factory} system property.
     */
    public static Routes routes() {
        return Routes.define(r -> {
            r.get("/todos").to(TodoController.class, "list");
            r.get("/todos/:id").to(TodoController.class, "show");
            r.post("/todos").to(TodoController.class, "create");
        }).compile();
    }

    /**
     * Builds the full {@link WebApplication} middleware stack.
     * Called by {@code KotowariFeature} at native-image build time to trigger
     * mixin class pre-generation via {@link enkan.util.MixinUtils#createFactory}.
     * The {@code injector} parameter is {@code null} at build time — only the
     * middleware registrations matter for mixin inference.
     */
    public static WebApplication buildApp() {
        WebApplication app = new WebApplication();

        ObjectMapper mapper = JsonMapper.builder()
                .enable(DeserializationFeature.UNWRAP_SINGLE_VALUE_ARRAYS)
                .disable(DeserializationFeature.FAIL_ON_IGNORED_PROPERTIES)
                .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
                .build();

        Routes routes = routes();

        app.use(new DefaultCharsetMiddleware());
        app.use(new ContentTypeMiddleware());
        app.use(new ParamsMiddleware());
        app.use(new CookiesMiddleware());
        app.use(new ContentNegotiationMiddleware());
        app.use(new ResourceMiddleware());
        app.use(new RoutingMiddleware(routes));
        app.use(builder(new SerDesMiddleware<>())
                .set(SerDesMiddleware::setBodyWriters,
                        new MessageBodyWriter[]{
                                new ToStringBodyWriter(),
                                new JsonBodyWriter<>(mapper)})
                .set(SerDesMiddleware::setBodyReaders,
                        new JsonBodyReader<>(mapper))
                .build());
        app.use(new NativeControllerInvokerMiddleware<>(null));

        return app;
    }

    @Override
    public Application<HttpRequest, HttpResponse> create(ComponentInjector injector) {
        WebApplication app = new WebApplication();

        ObjectMapper mapper = JsonMapper.builder()
                .enable(DeserializationFeature.UNWRAP_SINGLE_VALUE_ARRAYS)
                .disable(DeserializationFeature.FAIL_ON_IGNORED_PROPERTIES)
                .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
                .build();

        Routes routes = routes();

        // Register routes for KotowariFeature build-time discovery
        RouteRegistry.register(routes);

        app.use(new DefaultCharsetMiddleware());
        app.use(new ContentTypeMiddleware());
        app.use(new ParamsMiddleware());
        app.use(new CookiesMiddleware());
        app.use(new ContentNegotiationMiddleware());
        app.use(new ResourceMiddleware());
        app.use(new RoutingMiddleware(routes));
        app.use(builder(new SerDesMiddleware<>())
                .set(SerDesMiddleware::setBodyWriters,
                        new MessageBodyWriter[]{
                                new ToStringBodyWriter(),
                                new JsonBodyWriter<>(mapper)})
                .set(SerDesMiddleware::setBodyReaders,
                        new JsonBodyReader<>(mapper))
                .build());
        app.use(new NativeControllerInvokerMiddleware<>(injector));

        return app;
    }
}
