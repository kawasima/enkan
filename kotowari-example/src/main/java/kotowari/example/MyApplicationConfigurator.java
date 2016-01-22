package kotowari.example;

import enkan.Application;
import enkan.application.WebApplication;
import enkan.config.ApplicationConfigurator;
import enkan.middleware.HtmlRenderer;
import enkan.middleware.NormalizationMiddleware;
import enkan.middleware.ParamsMiddleware;
import enkan.middleware.TraceMiddleware;
import enkan.system.inject.ComponentInjector;
import kotowari.example.controller.ExampleController;
import kotowari.middleware.ControllerInvokerMiddleware;
import kotowari.middleware.FormMiddleware;
import kotowari.middleware.RoutingMiddleware;
import kotowari.middleware.ValidateFormMiddleware;
import kotowari.routing.Routes;

/**
 * @author kawasima
 */
public class MyApplicationConfigurator implements ApplicationConfigurator {
    @Override
    public void config(Application genericApp, ComponentInjector injector) {
        WebApplication app = (WebApplication) genericApp;

        // Enkan
        app.use(new TraceMiddleware<>());
        app.use(new ParamsMiddleware());
        app.use(new NormalizationMiddleware());

        Routes routes = Routes.define(r -> {
            r.get("/").to(ExampleController.class, "method1");
            r.get("/m2").to(ExampleController.class, "method2");
            r.get("/m3").to(ExampleController.class, "method3");
            r.get("/m4").to(ExampleController.class, "method5");
        }).compile();

        // Kotowari
        app.use(new RoutingMiddleware(routes));
        app.use(new FormMiddleware());
        app.use(new ValidateFormMiddleware());
        app.use(new HtmlRenderer());
        app.use(new ControllerInvokerMiddleware(injector));
    }
}
