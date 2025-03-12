package enkan.component;

import enkan.Application;
import enkan.Middleware;
import enkan.MiddlewareChain;
import enkan.chain.DefaultMiddlewareChain;
import enkan.config.ApplicationFactory;
import enkan.system.inject.ComponentInjector;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

/**
 * @author kawasima
 */
public class TestApplicationFactory implements ApplicationFactory<String, String> {
    @Override
    public Application<String, String> create(ComponentInjector injector) {
        Application<String, String> app = new Application<>() {
            private final List<MiddlewareChain<?, ?, ?, ?>> middlewares = new ArrayList<>();

            @Override
            public <REQ, RES, NREQ, NRES> void use(Predicate<? super REQ> predicate, String middlewareName, Middleware<REQ, RES, NREQ, NRES> middleware) {
                middlewares.add(new DefaultMiddlewareChain<>(predicate, middlewareName, middleware));
            }

            @Override
            public String handle(String s) {
                return null;
            }

            @Override
            public List<MiddlewareChain<?, ?, ?, ?>> getMiddlewareStack() {
                return middlewares;
            }
        };
        app.use(new Test1Middleware<>());
        return app;
    }
}
