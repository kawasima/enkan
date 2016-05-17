package kotowari.example;

import enkan.Application;
import enkan.Endpoint;
import enkan.Env;
import enkan.application.WebApplication;
import enkan.config.ApplicationFactory;
import enkan.data.HttpRequest;
import enkan.data.HttpResponse;
import enkan.endpoint.ResourceEndpoint;
import enkan.middleware.*;
import enkan.middleware.devel.HttpStatusCatMiddleware;
import enkan.middleware.devel.StacktraceMiddleware;
import enkan.middleware.devel.TraceWebMiddleware;
import enkan.middleware.doma2.DomaTransactionMiddleware;
import enkan.middleware.metrics.MetricsMiddleware;
import enkan.middleware.session.JCacheStore;
import enkan.middleware.session.KeyValueStore;
import enkan.middleware.session.MemoryStore;
import enkan.predicate.PathPredicate;
import enkan.security.backend.SessionBackend;
import enkan.system.inject.ComponentInjector;
import enkan.util.HttpResponseUtils;
import kotowari.example.controller.*;
import kotowari.example.controller.guestbook.GuestbookController;
import kotowari.example.controller.guestbook.LoginController;
import kotowari.middleware.*;
import kotowari.middleware.serdes.ToStringBodyWriter;
import kotowari.routing.Routes;

import java.util.Arrays;
import java.util.Objects;

import static enkan.util.BeanBuilder.builder;
import static enkan.util.Predicates.*;

/**
 * @author kawasima
 */
public class MyApplicationFactory implements ApplicationFactory {
    @Override
    public Application create(ComponentInjector injector) {
        WebApplication app = new WebApplication();

        // Routing
        Routes routes = Routes.define(r -> {
            r.get("/").to(ExampleController.class, "index");
            r.get("/m2").to(ExampleController.class, "method2");
            r.get("/m3").to(ExampleController.class, "method3");
            r.get("/m4").to(ExampleController.class, "method4");

            r.get("/guestbook/login").to(LoginController.class, "loginForm");
            r.post("/guestbook/login").to(LoginController.class, "login");
            r.get("/guestbook/").to(GuestbookController.class, "list");
            r.post("/guestbook/").to(GuestbookController.class, "post");

            r.get("/conversation/1").to(ConversationStateController.class, "page1");
            r.post("/conversation/2").to(ConversationStateController.class, "page2");
            r.post("/conversation/3").to(ConversationStateController.class, "page3");
            r.get("/misc/counter").to(MiscController.class, "counter");
            r.get("/misc/upload").to(MiscController.class, "uploadForm");
            r.post("/misc/upload").to(MiscController.class, "upload");
            r.get("/hospitality/unreachable").to(HospitalityDemoController.class, "unreachable");
            r.get("/hospitality/misconfiguration").to(HospitalityDemoController.class, "misconfiguration");
            r.resource(CustomerController.class);
            r.get("/customer/list").to(CustomerController.class, "list");
            r.post("/customer/validate").to(CustomerController.class, "validate");
        }).compile();

        // Enkan
        app.use(new DefaultCharsetMiddleware());
        app.use(new MetricsMiddleware<>());
        app.use(NONE, new ServiceUnavailableMiddleware<>(new ResourceEndpoint("/public/html/503.html")));
        app.use(envIn("development"), new StacktraceMiddleware());
        app.use(envIn("development"), new TraceWebMiddleware());
        app.use(new TraceMiddleware<>());
        app.use(new ContentTypeMiddleware());
        app.use(envIn("development"), new HttpStatusCatMiddleware());
        app.use(new ParamsMiddleware());
        app.use(new MultipartParamsMiddleware());
        app.use(new MethodOverrideMiddleware());
        app.use(new NormalizationMiddleware());
        app.use(new NestedParamsMiddleware());
        app.use(new CookiesMiddleware());

        KeyValueStore store = Objects.equals(Env.get("ENKAN_ENV"), "jcache") ? new JCacheStore() : new MemoryStore();
        app.use(builder(new SessionMiddleware())
                .set(SessionMiddleware::setStore, store)
                .build());
        app.use(PathPredicate.ANY("^/(guestbook|conversation)/.*"), new ConversationMiddleware());

        app.use(new AuthenticationMiddleware<>(Arrays.asList(new SessionBackend())));
        app.use(and(path("^/guestbook/"), authenticated().negate()),
                (Endpoint<HttpRequest, HttpResponse>) req ->
                        HttpResponseUtils.redirect("/guestbook/login?url=" + req.getUri(),
                                HttpResponseUtils.RedirectStatusCode.TEMPORARY_REDIRECT));

        app.use(new ContentNegotiationMiddleware());
        // Kotowari
        app.use(new ResourceMiddleware());
        app.use(new RenderTemplateMiddleware());
        app.use(new RoutingMiddleware(routes));
        app.use(new DomaTransactionMiddleware<>());
        app.use(new FormMiddleware());
        app.use(builder(new SerDesMiddleware())
                .set(SerDesMiddleware::setBodyWriters, new ToStringBodyWriter())
                .build());
        app.use(new ValidateFormMiddleware());
        app.use(new ControllerInvokerMiddleware(injector));

        return app;
    }
}
