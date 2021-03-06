package enkan.application;

import enkan.Application;
import enkan.Middleware;
import enkan.MiddlewareChain;
import enkan.chain.DefaultMiddlewareChain;
import enkan.data.HttpRequest;
import enkan.data.HttpResponse;
import enkan.data.UriAvailable;
import enkan.predicate.PathPredicate;
import enkan.util.Predicates;

import java.util.LinkedList;
import java.util.List;
import java.util.function.Predicate;

/**
 * An application handles HttpRequest/HttpResponse.
 *
 * @author kawasima
 */
public class WebApplication implements Application<HttpRequest, HttpResponse> {
    private final LinkedList<MiddlewareChain<?, ?, ?, ?>> middlewareStack = new LinkedList<>();

    public <REQ extends UriAvailable, RES, NREQ, NRES> void get(String path, Middleware<REQ, RES, NREQ, NRES> middleware) {
        use(PathPredicate.GET(path), middleware);
    }

    public <REQ extends UriAvailable, RES, NREQ, NRES> void post(String path, Middleware<REQ, RES, NREQ, NRES> middleware) {
        use(PathPredicate.POST(path), middleware);
    }
    public <REQ extends UriAvailable, RES, NREQ, NRES> void put(String path, Middleware<REQ, RES, NREQ, NRES> middleware) {
        use(PathPredicate.PUT(path), middleware);
    }
    public <REQ extends UriAvailable, RES, NREQ, NRES> void delete(String path, Middleware<REQ, RES, NREQ, NRES> middleware) {
        use(PathPredicate.DELETE(path), middleware);
    }

    @Override
    public <REQ, RES, NREQ, NRES> void use(Predicate<? super REQ> decision, String middlewareName, Middleware<REQ, RES, NREQ, NRES> middleware) {
        MiddlewareChain<REQ, RES, NREQ, NRES> chain = new DefaultMiddlewareChain<>(decision, middlewareName, middleware);
        if (!middlewareStack.isEmpty()) {
            middlewareStack.getLast().setNext(cast(chain));
        }
        middlewareStack.addLast(chain);
    }

    @Override
    public HttpResponse handle(HttpRequest req) {
        return new DefaultMiddlewareChain<HttpRequest, HttpResponse, HttpRequest, HttpResponse> (Predicates.any(), "bootstrap", (req1, chain) ->
                chain.next(req1)).setNext(cast(middlewareStack.getFirst())).next(req);
    }

    @Override
    public List<MiddlewareChain<?, ?, ?, ?>> getMiddlewareStack() {
        return middlewareStack;
    }

    /**
     * A cast helper of middleware chain
     *
     * @param chain the middleware chain without type parameters
     * @param <REQ> the type of request
     * @param <RES> the type of response
     * @return the middleware chain with type parameters
     */
    @SuppressWarnings("unchecked")
    private <REQ, RES> MiddlewareChain<REQ, RES, ?, ?> cast(MiddlewareChain chain) {
        return chain;
    }
}
