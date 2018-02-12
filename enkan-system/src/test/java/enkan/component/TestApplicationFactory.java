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
public class TestApplicationFactory implements ApplicationFactory {
    @Override
    public Application create(ComponentInjector injector) {
        Application<String, String> app = new Application<String, String>() {
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
