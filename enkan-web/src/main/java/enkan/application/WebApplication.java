package enkan.application;

import enkan.Application;
import enkan.Middleware;
import enkan.MiddlewareChain;
import enkan.chain.DefaultMiddlewareChain;
import enkan.data.HttpRequest;
import enkan.data.HttpResponse;
import enkan.predicate.PathPredicate;

import java.util.LinkedList;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * @author kawasima
 */
public class WebApplication implements Application<HttpRequest, HttpResponse> {
    private LinkedList<MiddlewareChain<?, ?>> middlewareStack = new LinkedList<>();

    public void get(String path, Middleware middleware) {
        use(PathPredicate.GET(path), middleware);
    }

    public void post(String path, Middleware middleware) {
        use(PathPredicate.POST(path), middleware);
    }
    public void put(String path, Middleware middleware) {
        use(PathPredicate.PUT(path), middleware);
    }
    public void delete(String path, Middleware middleware) {
        use(PathPredicate.DELETE(path), middleware);
    }

    @Override
    public <M_REQ, M_RES> void use(Predicate<M_REQ> decision, String middlewareName, Middleware<M_REQ, M_RES> middleware) {
        MiddlewareChain<M_REQ, M_RES> chain = new DefaultMiddlewareChain<>(decision, middlewareName, middleware);
        if (!middlewareStack.isEmpty()) {
            middlewareStack.getLast().setNext(chain);
        }
        middlewareStack.addLast(chain);
    }

    @Override
    public HttpResponse handle(HttpRequest req) {
        return (HttpResponse) new DefaultMiddlewareChain<>(ANY, "bootstrap", (req1, next) ->
                next.next(req1)).setNext(middlewareStack.getFirst()).next(req);
    }

    @Override
    public List<MiddlewareChain<?, ?>> getMiddlewareStack() {
        return middlewareStack;
    }
}
