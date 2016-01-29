package enkan.application;

import enkan.Application;
import enkan.Decision;
import enkan.Middleware;
import enkan.MiddlewareChain;
import enkan.data.HttpRequest;
import enkan.data.HttpResponse;
import enkan.decision.AllDecision;
import enkan.decision.PathDecision;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author kawasima
 */
public class WebApplication implements Application<HttpRequest, HttpResponse> {
    private LinkedList<MiddlewareChain> middlewareStack = new LinkedList<>();
    private static final AllDecision ANY = new AllDecision<>();

    public WebApplication() {
    }

    @Override
    public void use(Middleware middleware) {
        use(ANY, middleware);
    }

    public void get(String path, Middleware middleware) {
        use(PathDecision.GET(path), middleware);
    }

    public void post(String path, Middleware middleware) {
        use(PathDecision.POST(path), middleware);
    }
    public void put(String path, Middleware middleware) {
        use(PathDecision.PUT(path), middleware);
    }
    public void delete(String path, Middleware middleware) {
        use(PathDecision.DELETE(path), middleware);
    }

    @Override
    public <M_REQ, M_RES> void use(Decision<M_REQ> decision, Middleware<M_REQ, M_RES> middleware) {
        MiddlewareChain<M_REQ, M_RES> chain = new MiddlewareChain<>(decision, middleware);
        if (!middlewareStack.isEmpty()) {
            middlewareStack.getLast().setNext(chain);
        }
        middlewareStack.addLast(chain);
    }

    @Override
    public HttpResponse handle(HttpRequest req) {
        return (HttpResponse) new MiddlewareChain<>(ANY, (Middleware) (req1, next) ->
                next.next(req1)).setNext(middlewareStack.getFirst()).next(req);
    }

    @Override
    public List<Middleware> getMiddlewareStack() {
        return middlewareStack.stream()
                .map(chain -> chain.getMiddleware())
                .collect(Collectors.toList());
    }

    @Override
    public void clear() {
        middlewareStack.clear();
    }
}
