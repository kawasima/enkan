package enkan.component;

import enkan.Application;
import enkan.Middleware;
import enkan.MiddlewareChain;
import enkan.chain.DefaultMiddlewareChain;
import enkan.config.ApplicationFactory;
import enkan.system.inject.ComponentInjector;
import enkan.util.Predicates;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

public class TestCorrectApplicationFactory implements ApplicationFactory<String, String> {
    @Override
    public Application<String, String> create(ComponentInjector injector) {
        Application<String, String> app = new Application<>() {
            private final List<MiddlewareChain<?, ?, ?, ?>> middlewares = new ArrayList<>();

            @Override
            public <REQ, RES, NREQ, NRES> void use(Predicate<? super REQ> predicate, String middlewareName, Middleware<REQ, RES, NREQ, NRES> middleware) {
                middlewares.add(new DefaultMiddlewareChain<>(predicate, middlewareName, middleware));
            }

            @Override
            @SuppressWarnings("unchecked")
            public String handle(String s) {
                return new DefaultMiddlewareChain<>(Predicates.any(), "bootstrap",
                        new Middleware<String, String, Object, Object>() {
                            @Override
                            public <NNREQ, NNRES> String handle(String req, MiddlewareChain<Object, Object, NNREQ, NNRES> chain) {
                                return (String) chain.next(req);
                            }
                        }).setNext((MiddlewareChain<Object, Object, ?, ?>) middlewares.getFirst()).next(s);
            }

            @Override
            public List<MiddlewareChain<?, ?, ?, ?>> getMiddlewareStack() {
                return middlewares;
            }
        };
        app.use(new Test3Middleware<>());
        return app;
    }

}
