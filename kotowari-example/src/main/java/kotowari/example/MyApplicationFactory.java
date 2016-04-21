package kotowari.example;

import enkan.Application;
import enkan.Endpoint;
import enkan.application.WebApplication;
import enkan.config.ApplicationFactory;
import enkan.data.HttpRequest;
import enkan.data.HttpResponse;
import enkan.endpoint.ResourceEndpoint;
import enkan.middleware.*;
import enkan.middleware.doma2.DomaTransactionMiddleware;
import enkan.middleware.metrics.MetricsMiddleware;
import enkan.predicate.NonePredicate;
import enkan.security.backend.SessionBackend;
import enkan.system.inject.ComponentInjector;
import enkan.util.HttpResponseUtils;
import kotowari.example.controller.CustomerController;
import kotowari.example.controller.ExampleController;
import kotowari.example.controller.HospitalityDemoController;
import kotowari.example.controller.MiscController;
import kotowari.example.controller.guestbook.GuestbookController;
import kotowari.example.controller.guestbook.LoginController;
import kotowari.middleware.*;
import kotowari.routing.Routes;

import java.util.Arrays;

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
            r.get("/misc/counter").to(MiscController.class, "counter");
            r.get("/misc/upload").to(MiscController.class, "uploadForm");
            r.post("/misc/upload").to(MiscController.class, "upload");
            r.get("/hospitality/unreachable").to(HospitalityDemoController.class, "unreachable");
            r.get("/hospitality/misconfiguration").to(HospitalityDemoController.class, "misconfiguration");
            r.resource(CustomerController.class);
            r.get("/customer/list").to(CustomerController.class, "list");
        }).compile();

        // Enkan
        app.use(new DefaultCharsetMiddleware());
        app.use(new MetricsMiddleware<>());
        app.use(new NonePredicate(), new ServiceUnavailableMiddleware<>(new ResourceEndpoint("/public/html/503.html")));
        app.use(new StacktraceMiddleware());
        app.use(new TraceMiddleware<>());
        app.use(new ContentTypeMiddleware());
        app.use(new HttpStatusCatMiddleware());
        app.use(new ParamsMiddleware());
        app.use(new MultipartParamsMiddleware());
        app.use(new MethodOverrideMiddleware());
        app.use(new NormalizationMiddleware());
        app.use(new NestedParamsMiddleware());
        app.use(new CookiesMiddleware());
        app.use(new SessionMiddleware());

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
        app.use(new SerDesMiddleware());
        app.use(new ValidateFormMiddleware());
        app.use(new ControllerInvokerMiddleware(injector));

        return app;
    }
}
